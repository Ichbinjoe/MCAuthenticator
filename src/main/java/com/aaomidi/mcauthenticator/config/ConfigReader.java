package com.aaomidi.mcauthenticator.config;

import com.aaomidi.mcauthenticator.util.StringManager;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by amir on 2016-01-11.
 */
public class ConfigReader {
    @Setter
    private static FileConfiguration fileConfiguration;
    private static String prefix;
    private static String serverIP;
    private static String staffPermission;
    private static String lockPermission;
    private static Boolean mapBasedQR;

    public static String getPrefix() {
        if (prefix == null) {
            prefix = fileConfiguration.getString("Prefix");
        }
        return prefix;
    }

    public static String getStaffPermission() {
        if (staffPermission == null) {
            staffPermission = fileConfiguration.getString("Permissions.Staff");
        }
        return staffPermission;
    }

    public static String getLockPermission() {
        if (lockPermission == null) {
            lockPermission = fileConfiguration.getString("Permissions.Lock");
        }
        return lockPermission;
    }

    public static String getServerIP() {
        if (serverIP == null) {
            serverIP = fileConfiguration.getString("Server-IP");
        }
        return serverIP;
    }

    public static void reload(FileConfiguration newConfig) {
        fileConfiguration = newConfig;
        prefix = null;
        serverIP = null;
        staffPermission = null;
        lockPermission = null;
        mapBasedQR = null;
        StringManager.setPrefix(getPrefix());
    }

    public static boolean useMapQR(){
        if(mapBasedQR == null){
            mapBasedQR = fileConfiguration.getBoolean("UseMapBasedQR", false);
        }
        return mapBasedQR;
    }
}
