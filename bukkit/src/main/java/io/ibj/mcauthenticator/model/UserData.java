package io.ibj.mcauthenticator.model;

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
    int getAuthType();
    void setSecret(String secret, int authtype);
    boolean isLocked(Player player);
    void setLocked(boolean lock);
}
