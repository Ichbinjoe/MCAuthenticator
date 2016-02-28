package com.aaomidi.mcauthenticator;

import com.aaomidi.mcauthenticator.auth.Authenticator;
import com.aaomidi.mcauthenticator.auth.RFC6238;
import com.aaomidi.mcauthenticator.auth.Yubikey;
import com.aaomidi.mcauthenticator.model.UserDataSource;
import com.aaomidi.mcauthenticator.model.datasource.DirectoryUserDataSource;
import com.aaomidi.mcauthenticator.model.datasource.MySQLUserDataSource;
import com.aaomidi.mcauthenticator.model.datasource.SingleFileUserDataSource;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
@Getter
public final class Config {

    private final String serverIP;
    @Getter
    private final UserDataSource dataSource;

    @Getter
    private final Authenticator internalAuthenticator;

    @Getter
    private boolean enforceSameIPAuth;

    private final Map<String, String> messages;
    private String prefix = color("&8[&4Auth&8] ");

    public Config(MCAuthenticator auth, ConfigurationSection section) throws SQLException, IOException {
        String authenticator = section.getString("authenticator", "2fa");
        if (authenticator.equalsIgnoreCase("2fa")) {
            internalAuthenticator = new RFC6238(auth);
            auth.getLogger().info("Using RFC6238 (Google Authenticator 2FA) based authentication.");
        } else if (authenticator.equalsIgnoreCase("yubikey")) {
            Integer clientId = section.getInt("yubikey.clientId");
            String clientSecret = section.getString("yubikey.clientSecret");
            if (clientSecret == null) {
                auth.getLogger().info("The Yubikey configuration section does not appear to be set up correctly! Both clientId" +
                        " and clientSecret must be filled in correctly! Otherwise, the authenticator may not function correctly.");
                auth.getLogger().info("If you do not know these, they can be retrieved from here: https://upgrade.yubico.com/getapikey/");
            }
            internalAuthenticator = new Yubikey(clientId, clientSecret);
            auth.getLogger().info("Using Yubikey based authenticator.");
        } else {
            auth.getLogger().info("You did not specify a proper authenticator! (2fa/yubikey). Please fix this, however," +
                    " we are falling back to 2fa.");
            internalAuthenticator = new RFC6238(auth);
        }
        if (internalAuthenticator instanceof RFC6238) {
            String tempServerIP;
            tempServerIP = section.getString("serverIp");
            if (tempServerIP == null) {
                auth.getLogger().info("Your serverIp within your MCAuthenticator configuration is not set! It defaults " +
                        "'MCAuthenticator', but you should consider changing it to your server name!");
                tempServerIP = "MCAuthenticator";
            }
            this.serverIP = tempServerIP;
        } else {
            this.serverIP = null; //Its not needed for other modes.
        }
        String backing = section.getString("dataBacking.type", "single");
        switch (backing) {
            case "single":
                this.dataSource = new SingleFileUserDataSource(new File(auth.getDataFolder(), section.getString("dataBacking.file", "playerData.json")));
                break;
            case "directory":
                this.dataSource = new DirectoryUserDataSource(new File(auth.getDataFolder(), section.getString("dataBacking.directory", "playerData")));
                break;
            case "mysql":
                ConfigurationSection mysql = section.getConfigurationSection("dataBacking.mysql");
                this.dataSource = new MySQLUserDataSource(mysql.getString("url", "jdbc:mysql://localhost:3306/db"),
                        mysql.getString("username"),
                        mysql.getString("password"));
                break;
            default:
                throw new IllegalArgumentException("The dataBacking type '" + backing + "' doesn't exist.");
        }

        auth.getLogger().info("Using data source: " + dataSource.toString());

        this.enforceSameIPAuth = section.getBoolean("forceSameIPAuthentication", false);

        this.messages = new HashMap<>();
        ConfigurationSection msgCfg = section.getConfigurationSection("messages");
        for (String key : msgCfg.getKeys(false)) {
            if (key.equals("prefix")) {
                this.prefix = color(msgCfg.getString(key));
            }
            this.messages.put(key, color(msgCfg.getString(key)));
        }
    }

    public String message(String key) {
        return prefix + messages.get(key);
    }

    public void send(CommandSender sender, String message) {
        sender.sendMessage(message.split("\n"));
    }

    public void sendDirect(CommandSender sender, String raw) {
        send(sender, prefixFormat(raw));
    }

    public String prefixFormat(String s) {
        return prefix + color(s);
    }

    private String color(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }


}
