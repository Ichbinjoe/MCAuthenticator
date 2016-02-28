package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.auth.Authenticator;
import com.aaomidi.mcauthenticator.auth.RFC6238;
import com.aaomidi.mcauthenticator.auth.Yubikey;
import com.aaomidi.mcauthenticator.map.ImageMapRenderer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.yubico.client.v2.YubicoClient;
import lombok.Getter;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.net.InetAddress;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public final class User {

    public User(UserData userData, MCAuthenticator mcAuthenticator) {
        this.userData = userData;
        this.mcAuthenticator = mcAuthenticator;
    }

    private UserData userData;
    private final MCAuthenticator mcAuthenticator;

    //Whether or not the user is viewed as 'authenticated'
    private boolean authenticated = false;
    @Getter
    private boolean isFirstTime = false;
    /*
    Map QR Logic
     */
    @Getter
    private boolean isViewingQRCode = false;

    private ItemStack[] inventory = null;

    public boolean authenticated() {
        return userData == null || authenticated;
    }

    public boolean mustSetUp2FA() {
        return userData != null && userData.getSecret() == null;
    }

    public boolean authenticate(InetAddress address) {
        if (authenticated()) return true;
        if (!mcAuthenticator.getC().isEnforceSameIPAuth() &&
                userData.getLastAddress() != null && userData.getLastAddress().equals(address))
            authenticated = true;
        return authenticated;
    }

    public boolean is2fa() {
        return userData != null;
    }

    public boolean authenticate(String message, Player player) throws Exception {
        if (!mcAuthenticator.getAuth().authenticate(userData.getSecret(), message)) return false; //Bad code

        authenticated = true;

        if (isViewingQRCode)
            stopViewingQRMap(player);
        if (isFirstTime && mcAuthenticator.getAuth() instanceof Yubikey) {
            //Need to set our internal key
            userData.setSecret(YubicoClient.getPublicId(message));
        }
        userData.setLastAddress(player.getAddress().getAddress());
        userData.setLocked(player.hasPermission("mcauthenticator.lock"));
        mcAuthenticator.save();
        reverseInventory(player);
        return true;
    }

    public void invalidateKey() {
        userData.setSecret(null);
        authenticated = false;
    }

    public boolean init2fa(Player p) {
        if (userData == null) {
            userData = mcAuthenticator.getDataSource().createUser(p.getUniqueId());
        } else if (userData.getSecret() != null) { //Already has a secret
            return false;
        }

        Authenticator a = mcAuthenticator.getAuth();
        if (a instanceof RFC6238) {
            userData.setSecret(((RFC6238) a).createNewKey());
            mcAuthenticator.getC().send(p, mcAuthenticator.getC().message("sendAuthCode").replaceAll("%code%", userData.getSecret())
                    .replaceAll("%url%", ((RFC6238) a).getQRUrl(p.getName(), userData.getSecret())));
            ImageMapRenderer mapRenderer;
            try {
                mapRenderer = new ImageMapRenderer(p.getName(), userData.getSecret(), mcAuthenticator.getC().getServerIP());
            } catch (WriterException e) {
                mcAuthenticator.getC().sendDirect(p, "&cThere was an error rendering your 2FA QR code!");
                mcAuthenticator.handleException(e);
                return true;
            }

            storeInventory(p);
            isViewingQRCode = true;

            ItemStack itemStack = new ItemStack(Material.MAP);
            MapView map = Bukkit.createMap(p.getWorld());
            itemStack.setDurability(map.getId());
            itemStack.setAmount(0);
            p.getInventory().setHeldItemSlot(0);
            p.setItemInHand(itemStack);

            Location playerLocation = p.getLocation();
            playerLocation.setPitch(90);
            p.teleport(playerLocation);

            for (MapRenderer r : map.getRenderers()) {
                map.removeRenderer(r);
            }

            map.addRenderer(mapRenderer);
            p.sendMap(map);
        } else {
            userData.setSecret(null); //Allow for the secret to be picked up by the auth unit
            mcAuthenticator.getC().send(p, mcAuthenticator.getC().message("yubikeyInitial"));
        }

        isFirstTime = true;

        mcAuthenticator.save();

        return true;
    }

    public void stopViewingQRMap(Player p) {
        reverseInventory(p);
        isViewingQRCode = false;
    }

    public void logout(Player p) {
        if (isViewingQRCode)
            stopViewingQRMap(p);

        reverseInventory(p);
        if (isFirstTime && !authenticated)
        //Take out user, since they never really authenticated. This way they can join again.
        {
            mcAuthenticator.getDataSource().destroyUser(p.getUniqueId());
            mcAuthenticator.save();
        }
    }

    public boolean isLocked(Player p) {
        return userData != null && userData.isLocked(p);
    }

    public void storeInventory(Player p) {
        if (inventory != null) throw new IllegalStateException("Cannot double store inventory!");
        inventory = p.getInventory().getContents();
        p.getInventory().setContents(new ItemStack[36]);
    }

    public void reverseInventory(Player p) {
        if (inventory != null) {
            p.getInventory().setContents(inventory);
            inventory = null;
        }
    }

    public void disable(Player p) {
        if (userData == null) return;
        if (isViewingQRCode) {
            stopViewingQRMap(p);
        } else { //Lets still try to reverse
            reverseInventory(p);
        }

        mcAuthenticator.getDataSource().destroyUser(userData.getId());
        userData = null;
        mcAuthenticator.save();
    }

}
