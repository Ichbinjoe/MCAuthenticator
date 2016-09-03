package com.aaomidi.mcauthenticator.model.datasource;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.UserData;
import com.aaomidi.mcauthenticator.model.UserDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/30/16
 */
public final class DirectoryUserDataSource implements UserDataSource {

    private Set<UpdatableFlagData> update = new HashSet<>();
    private Set<UUID> delete = new HashSet<>();
    private final UpdateHook updateHook;
    private final File parentDirectory;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String toString() {
        return "(DirectoryDataSource: "+parentDirectory.getPath()+")";
    }

    public DirectoryUserDataSource(File parentDirectory) throws IOException {
        this.parentDirectory = parentDirectory;
        if (!parentDirectory.isDirectory()) {
            if (!parentDirectory.mkdirs()) {
                throw new IOException("Failed to create directory "+parentDirectory.getPath());
            }
        }
        this.updateHook = new UpdateHook() {
            @Override
            public void update(UpdatableFlagData me) {
                DirectoryUserDataSource.this.update.add(me);
            }
        };
    }

    @Override
    public UserData getUser(UUID id) throws IOException {
        if (Bukkit.isPrimaryThread() && !MCAuthenticator.isReload) throw new RuntimeException("Primary thread I/O");
        File f = getUserFile(id);
        if (!f.exists()) return null;

        try (FileReader reader = new FileReader(f)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonElement lastIp = jsonObject.get("lastIp");
            JsonElement secret = jsonObject.get("secret");
            JsonElement authType = jsonObject.get("authtype");
            return new UpdatableFlagData(updateHook,
                    UUID.fromString(jsonObject.get("id").getAsString()),
                    lastIp != null ? InetAddress.getByName(lastIp.getAsString()) : null,
                    secret != null ? secret.getAsString() : null,
                    authType != null ? authType.getAsInt(): 0,
                    jsonObject.get("locked").getAsBoolean());
        }
    }

    @Override
    public UserData createUser(UUID id) {
        UpdatableFlagData d = new UpdatableFlagData(updateHook, id, null, null,
                -1, false);
        update.add(d);
        return d;
    }

    @Override
    public void destroyUser(UUID id) {
        delete.add(id);
    }

    @Override
    public void save() throws IOException {
        Set<UpdatableFlagData> oldData = update;
        update = new HashSet<>();
        Set<UUID> oldDelete = delete;
        delete = new HashSet<>();

        Iterator<UpdatableFlagData> a = oldData.iterator();
        while (a.hasNext()) {
            if (delete.contains(a.next().getId())) a.remove();
        }
        for (UpdatableFlagData d : oldData) saveData(d);

        for (UUID d : oldDelete) getUserFile(d).delete();
    }

    private void saveData(UpdatableFlagData d) throws IOException {
        File f = getUserFile(d.getId());
        if (!f.exists()) {
            f.createNewFile();
        }

        JsonObject o = new JsonObject();
        o.addProperty("id", d.getId().toString());
        o.addProperty("secret", d.getSecret());
        o.addProperty("lastIp", d.getLastAddress() != null ? d.getLastAddress().getHostAddress() : null);
        o.addProperty("locked", d.isLocked(null));
        o.addProperty("authtype", d.getAuthType());
        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(o, writer);
        }
    }

    @Override
    public void invalidateCache() throws IOException {
        //We don't have a cache. Don't worry about it.
    }

    private File getUserFile(UUID id) {
        return new File(parentDirectory, id.toString() + ".json");
    }
}
