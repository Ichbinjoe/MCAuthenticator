package com.aaomidi.mcauthenticator.util;


import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by amir on 2015-11-29.
 */
public class StringManager {
    @Setter
    private static String prefix = "&7[&cAuthenticator&7]&r ";
    @Setter
    private static Logger logger;


    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void log(Level level, String s, Object... format) {
        String msg = String.format("%s: %s", prefix, String.format(s, format));
        logger.log(level, msg);
    }

    public static void sendMessage(CommandSender c, String s, Object... format) {
        String message = String.format(prefix + s, format);

        c.sendMessage(colorize(message));
    }
}
