package com.aaomidi.mcauthenticator.engine;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.DataFile;
import com.aaomidi.mcauthenticator.model.User;
import com.google.gson.Gson;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by amir on 2016-01-11.
 */
public class DataManager {
    private final MCAuthenticator instance;
    @Getter
    private DataFile dataFile;
    private File file;

    public DataManager(MCAuthenticator instance) {
        this.instance = instance;
        this.readDatafiles();
    }

    public void reload() {
        this.dataFile = null;
        this.readDatafiles();
    }

    private void readDatafiles() {
        file = new File(instance.getDataFolder(), "users.json");
        if (!file.exists()) {
            this.createFile(file);
        }
        Gson gson = new Gson();

        try {
            FileReader fileReader = new FileReader(file);
            dataFile = gson.fromJson(fileReader, DataFile.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (dataFile == null) {
            dataFile = new DataFile(new ArrayList<>());
        }
    }

    private void createFile(File file) {
        try {
            Gson gson = new Gson();
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            List<User> list = new ArrayList<>();

            list.add(new User(UUID.randomUUID()));
            list.add(new User(UUID.randomUUID()));

            DataFile dataFile = new DataFile(list);
            fileWriter.write(gson.toJson(dataFile));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFile() {
        try {
            Gson gson = new Gson();

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(dataFile));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
