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

    public static List<String> colorizeList(List<String> list) {
        List<String> colorized = new ArrayList<>(list.size());
        list.forEach(s -> colorized.add(colorize(s)));
        return colorized;
    }

    public static void log(Level level, String s, Object... format) {
        String msg = String.format("%s: %s", prefix, String.format(s, format));
        logger.log(level, msg);
    }

    public static void log(String s) {
        String msg = prefix + ": " + s;
        System.out.print(msg);
    }

    public static void sendMessage(CommandSender c, String s, Object... format) {
        String message = String.format(prefix + s, format);

        c.sendMessage(colorize(message));
    }

    public static void sendMessageWithoutColor(CommandSender c, String message) {
        message = colorize(prefix) + message;
        c.sendMessage(message);
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;

        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }

        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;


    }

    // Example implementation of the Levenshtein Edit Distance
    private static int editDistance(String s1, String s2) {
        return StringUtils.getLevenshteinDistance(s1, s2);
    }
}
