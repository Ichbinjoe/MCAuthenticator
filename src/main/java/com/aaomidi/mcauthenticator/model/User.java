package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.auth.Authenticator;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public final class User {

    public User(UUID playerId, UserData userData,
                MCAuthenticator mcAuthenticator) {
        this.playerId = playerId;
        this.userData = userData;
        this.mcAuthenticator = mcAuthenticator;
    }

    private final UUID playerId;

    @Getter
    private UserData userData;
    private final MCAuthenticator mcAuthenticator;

    @Getter
    private boolean isInit = false;

    //Whether or not the user is viewed as 'authenticated'
    private boolean authenticated = false;

    private ItemStack[] inventory = null;

    public boolean authenticated() {
        return !isInit && (userData == null || authenticated);
    }

    public boolean mustSetUp2FA() {
        return userData != null && userData.getSecret() == null;
    }

    public boolean authenticate(InetAddress address) {
        if (authenticated()) return true;
        if (!mcAuthenticator.getC().isEnforceSameIPAuth() &&
                userData.getLastAddress() != null &&
                userData.getLastAddress().equals(address)) {
            isInit = false;
            authenticated = true;
        }
        return authenticated;
    }

    public boolean is2fa() {
        return userData != null;
    }

    public boolean authenticate(String message, Player player)
            throws Exception {
        boolean authenticate = false;
        for(Authenticator a : mcAuthenticator.getAuthenticators()) {
            if(a.isFormat(message)) {
                authenticate = a.authenticate(this, message);
                if(authenticate) break;
            }
        }

        if(!authenticate) return false;

        isInit = false;
        authenticated = true;

        reverseInventory(player);
        userData.setLastAddress(player.getAddress().getAddress());
        userData.setLocked(player.hasPermission("mcauthenticator.lock"));
        mcAuthenticator.save();
        return true;
    }

    public void invalidateKey() {
        mcAuthenticator.getDataSource().destroyUser(userData.getId());
        userData = null;
        authenticated = false;
    }

    public boolean init2fa(Player p) {
        if (userData != null && userData.getSecret() != null) { //Already has a secret
            return false;
        }

        for (Authenticator a : mcAuthenticator.getAuthenticators()) {
            a.initUser(this, p);
        }

        isInit = true;

        return true;
    }

    public void setUserInfo(String secret, int authtype) {
        if(userData == null)
            userData = mcAuthenticator.getDataSource().createUser(playerId);
        userData.setSecret(secret, authtype);
    }

    public boolean isLocked(Player p) {
        return userData != null && userData.isLocked(p);
    }

    public boolean isInventoryStored() {
        return inventory != null;
    }

    public void storeInventory(Player p) {
        if (inventory != null)
            throw new IllegalStateException("Cannot double store inventory!");
        inventory = p.getInventory().getContents();
        p.getInventory().setContents(new ItemStack[36]);
    }

    public void reverseInventory(Player p) {
        if (inventory != null) {
            p.getInventory().setContents(inventory);
            inventory = null;
        }
    }
}
