package com.aaomidi.mcauthenticator;

import com.aaomidi.mcauthenticator.engine.CommandHandler;
import com.aaomidi.mcauthenticator.engine.events.ChatEvent;
import com.aaomidi.mcauthenticator.engine.events.ConnectionEvent;
import com.aaomidi.mcauthenticator.engine.events.InventoryEvent;
import com.aaomidi.mcauthenticator.engine.events.MoveEvent;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.model.UserCache;
import com.aaomidi.mcauthenticator.model.UserData;
import com.aaomidi.mcauthenticator.model.UserDataSource;
import com.aaomidi.mcauthenticator.model.datasource.SingleFileUserDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by amir on 2016-01-11.
 */
public final class MCAuthenticator extends JavaPlugin {

    @Getter
    private CommandHandler commandHandler;

    @Getter
    private Config c;
    private File configurationFile;

    public UserDataSource getDataSource() {
        return c.getDataSource();
    }

    @Getter
    private final UserCache cache = new UserCache(this);

    @Override
    public void onEnable() {
        commandHandler = new CommandHandler(this);
        getCommand("auth").setExecutor(commandHandler);
        commandHandler.registerCommands();

        registerEvent(new ChatEvent(this));
        registerEvent(new ConnectionEvent(this));
        registerEvent(new MoveEvent(this));
        registerEvent(new InventoryEvent(this));

        this.configurationFile = new File(getDataFolder(), "config.yml");
        reload();
    }

    private void registerEvent(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    public void handlePlayer(Player player, UserData data) throws IOException, SQLException {
        User user = this.getCache().join(player.getUniqueId(), data);
        if (user.authenticated()) {
            if (player.hasPermission("mcauthenticator.lock")) {
                //They have no userData, but they should have 2fa forced.
                user.init2fa(player);
            }
        } else {
            if (user.mustSetUp2FA()) {
                user.init2fa(player);
            } else {
                if (!user.authenticate(player.getAddress().getAddress())) {
                    //User must enter code
                    user.storeInventory(player);
                    c.send(player, c.message("authenticationPrompt"));
                } else {
                    c.send(player, c.message("ipPreAuthenticated"));
                }
            }
        }
    }

    public void reload() {
        if (!configurationFile.exists()) {
            getDataFolder().mkdirs();
            saveResource(configurationFile.getName(), false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configurationFile);
        try {
            this.c = new Config(this, cfg);
        } catch (SQLException | IOException e) {
            getLogger().log(Level.SEVERE, "There was an issue configuring the data source!", e);
            return;
        }

        cache.invalidate();
        loadAllPlayers();
    }

    public void loadAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                handlePlayer(p, getDataSource().getUser(p.getUniqueId()));
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "There was an error loading player " + p.getName(), e);
            }
        }
    }

    public void async(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(this, r);
    }

    public void sync(Runnable r) {
        Bukkit.getScheduler().runTask(this, r);
    }

    public void save() {
        async(new Runnable() {
            @Override
            public void run() {
                try {
                    getDataSource().save();
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "There was an error saving the datasource: ", e);
                }
            }
        });
    }
}
