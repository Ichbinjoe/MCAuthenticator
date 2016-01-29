package com.aaomidi.mcauthenticator;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
@Getter
public class Config {

    private final String serverIP;
    private final String force2faPerm;
    private final String lock2faPerm;
    private final String reset2fa;
    private final boolean mapBasedQR;

    private final Map<String, String> messages;
    private String prefix = color("&8[&4Auth&8] ");

    public Config(ConfigurationSection section, Logger configurationLogger) {
        String tempServerIP;
        tempServerIP = section.getString("serverIp");
        if (tempServerIP == null) {
            configurationLogger.info("Your serverIp within your MCAuthenticator configuration is not set! It defaults " +
                    "'MCAuthenticator', but you should consider changing it to your server name!");
            tempServerIP = "MCAuthenticator";
        }
        this.serverIP = tempServerIP;
        this.force2faPerm = section.getString("permissions.2fa", "mcauthenticator.2fa");
        this.lock2faPerm = section.getString("permissions.forceLock", "mcauthenticator.2faLock");
        this.reset2fa = section.getString("permissions.reset2fa", "mcauthenticator.reset");
        this.mapBasedQR = section.getBoolean("useMapBasedQR", true);

        this.messages = new HashMap<>();
        ConfigurationSection msgCfg = section.getConfigurationSection("messages");
        for (String key : msgCfg.getKeys(false)) {
            if(key.equals("prefix")){
                this.prefix = color(msgCfg.getString(key));
            }
            this.messages.put(key, color(msgCfg.getString(key)));
        }
    }

    public String message(String key){
        return prefix + messages.get(key);
    }

    private String color(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }


}
