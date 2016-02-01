package com.aaomidi.mcauthenticator.model;

import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public interface UserData {
    UUID getId();
    InetAddress getLastAddress();
    void setLastAddress(InetAddress inetAddress);
    String getSecret();
    void setSecret(String secret);
    boolean isLocked(Player player);
    void setLocked(boolean lock);
}
