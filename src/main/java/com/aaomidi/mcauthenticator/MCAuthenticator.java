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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
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

    public static boolean isReload = false;

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
        isReload = true;
        if (!configurationFile.exists()) {
            getDataFolder().mkdirs();
            saveResource(configurationFile.getName(), false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configurationFile);
        try {
            this.c = new Config(this, cfg);
        } catch (SQLException | IOException e) {
            handleException(e);
            return;
        }

        cache.invalidate();
        loadAllPlayers();
        isReload = false;
    }

    public void loadAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                handlePlayer(p, getDataSource().getUser(p.getUniqueId()));
            } catch (Exception e) {
                handleException(e);
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
                    handleException(e);
                }
            }
        });
    }

    public void handleException(Exception e) {
        if (e instanceof SQLTimeoutException) {
            getLogger().log(Level.SEVERE, "The backing datasource has had an SQL timeout: this is not a plugin issue." +
                    " Please ensure that your plugin has the correct SQL server address configured, and that the server" +
                    " is running and is capable of receiving connections from your server.", e);
        } else if (e instanceof SQLException) {
            if (e.getMessage().startsWith("Access denied")) {
                getLogger().log(Level.SEVERE, "It appears that the datasource has denied your SQL credentials: this is not a plugin issue." +
                        " Please ensure that your username and password for the server are configured correctly, and that the configured" +
                        " account has access to the database you have configured MCAuthenticator to use.", e);
            } else {
                getLogger().log(Level.SEVERE, "MCAuthenticator has encountered a general SQL error. Please review the error" +
                        " before submitting it as a bug.", e);
            }
        } else if (e instanceof ConnectException) {
            getLogger().log(Level.SEVERE, "The backing datasource refused to allow MCAuthenticator to connect to it:" +
                    " this is not a plugin issue. Please ensure that MCAuthenticator is configured to use the correct" +
                    " address, that the SQL server is running, and is capable of receiving connections from your server.", e);
        } else if (e instanceof IOException) {
            getLogger().log(Level.SEVERE, "An I/O exception has occurred. This can be caused by many things, and should be" +
                    " reviewed before submitting it as a bug.", e);
        } else {
            getLogger().log(Level.SEVERE, "An exception occurred. At the time, we do not have any context to why this error is" +
                    " occurring.", e);
        }
    }
}
