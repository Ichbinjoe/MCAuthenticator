package com.aaomidi.mcauthenticator.model.datasource;

import com.aaomidi.mcauthenticator.model.UserData;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
public class BasicUserData implements UserData {

    private final UUID id;
    private InetAddress inetAddress;
    private String secret;
    private boolean locked;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public InetAddress getLastAddress() {
        return inetAddress;
    }

    @Override
    public void setLastAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public boolean isLocked(Player p) {
        if (p == null)
            return locked;
        else
            return p.hasPermission("mcauthenticator.lock");
    }

    @Override
    public void setLocked(boolean lock) {
        this.locked = lock;
    }
}
