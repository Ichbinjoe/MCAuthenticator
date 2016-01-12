package com.aaomidi.mcauthenticator;

import com.aaomidi.mcauthenticator.config.ConfigReader;
import com.aaomidi.mcauthenticator.engine.CommandHandler;
import com.aaomidi.mcauthenticator.engine.DataManager;
import com.aaomidi.mcauthenticator.engine.events.*;
import com.aaomidi.mcauthenticator.map.ImageMapRenderer;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.util.StringManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by amir on 2016-01-11.
 */
public class MCAuthenticator extends JavaPlugin {
    @Getter
    private DataManager dataManager;
    @Getter
    private CommandHandler commandHandler;
    @Getter
    private ImageMapRenderer imageMapRenderer;

    @Override
    public void onLoad() {
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
        }
        ConfigReader.setFileConfiguration(this.getConfig());
        dataManager = new DataManager(this);
        StringManager.setPrefix(ConfigReader.getPrefix());
        StringManager.setLogger(this.getLogger());
    }

    @Override
    public void onEnable() {
        commandHandler = new CommandHandler(this);
        commandHandler.registerCommands();
        this.setupEvents();
    }


    private void setupEvents() {
        registerEvent(new ChatEvent(this));
        registerEvent(new ConnectionEvent(this));
        registerEvent(new MoveEvent(this));
        registerEvent(new InventoryEvent(this));
    }

    private void registerEvent(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    public void handlePlayer(Player player) {

        boolean lock = false;
        if (!player.hasPermission(ConfigReader.getStaffPermission())) {
            return;
        }

        if (player.hasPermission(ConfigReader.getLockPermission())) {
            lock = true;
        }

        User user = this.getDataManager().getDataFile().getUser(player.getUniqueId());
        if (user == null) {
            user = new User(player.getUniqueId());
            this.getDataManager().getDataFile().addUser(user);
        }
        user.setLocked(lock);
        user.setAuthenticated(false);
    }
}
