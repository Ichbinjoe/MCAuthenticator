package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.map.ImageMapRenderer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
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

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public class User {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private static final transient String googleFormat = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
    private static final transient String encodeFormat = "otpauth://totp/%s@%s?secret=%s";

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

    public boolean authenticate(String message, Player player) {
        int code;
        try {
            code = Integer.valueOf(message);

        } catch (NumberFormatException ex) {
            return false; //Not the passcode
        }

        if (!gAuth.authorize(userData.getSecret(), code)) return false; //Bad code

        authenticated = true;

        if (isViewingQRCode)
            stopViewingQRMap(player);
        userData.setLastAddress(player.getAddress().getAddress());
        userData.setLocked(player.hasPermission("mcauthenticator.lock"));
        mcAuthenticator.save();
        reverseInventory(player);
        return true;
    }

    public void createNewKey() {
        if (userData.getSecret() != null) {
            throw new IllegalStateException("A secret is already in place! In order to reset this key, invalidate it first. " +
                    "This prevents against locking a user out.");
        }
        userData.setSecret(gAuth.createCredentials().getKey());
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
        createNewKey();
        mcAuthenticator.getC().send(p, mcAuthenticator.getC().message("sendAuthCode").replaceAll("%code%", userData.getSecret())
                .replaceAll("%url%",createQRURL(p.getName())));
        ImageMapRenderer mapRenderer;
        try {
            mapRenderer = new ImageMapRenderer(this, p.getName());
        } catch (WriterException e) {
            e.printStackTrace();
            mcAuthenticator.getC().sendDirect(p, "&cThere was an error rendering your 2FA QR code!");
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
        isFirstTime = true;

        mcAuthenticator.save();

        return true;
    }

    public void sendFancyQRMessage(Player p) {
        new FancyMessage()
                .text("Click here")
                .color(ChatColor.GREEN)
                .style(ChatColor.UNDERLINE)
                .link(createQRURL(p.getName()))
                .then()
                .text(" for the ")
                .color(ChatColor.YELLOW)
                .then()
                .text("QR")
                .color(ChatColor.YELLOW)
                .style(ChatColor.BOLD)
                .then()
                .text(" code!")
                .color(ChatColor.YELLOW).send(p);
    }

    public String createQRURL(String username) {
        if (userData.getSecret() == null)
            return null;
        return String.format(googleFormat, username, mcAuthenticator.getC().getServerIP(), userData.getSecret());
    }

    public BitMatrix createQRBitmatrix(String username) throws WriterException {
        return new QRCodeWriter().encode(String.format(encodeFormat,
                username,
                mcAuthenticator.getC().getServerIP(),
                userData.getSecret()),
                BarcodeFormat.QR_CODE, 128, 128);
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
