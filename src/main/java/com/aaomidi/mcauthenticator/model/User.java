package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.config.ConfigReader;
import com.aaomidi.mcauthenticator.map.ImageMapRenderer;
import com.aaomidi.mcauthenticator.util.StringManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public class User {
    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private InetAddress inetAddress;
    @Getter
    @Setter
    private String secret;
    @Getter
    @Setter
    private boolean locked;

    @Setter
    @Getter
    private transient boolean firstTime = false;

    @Getter
    private static final transient String googleFormat = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
    private static final transient String encodeFormat = "otpauth://totp/%s@%s?secret=%s";

    private transient GoogleAuthenticator googleAuthenticator;
    @Getter
    @Setter
    private transient boolean authenticated = false;
    private transient FancyMessage fancyMessage = null;

    @Getter
    private transient boolean isViewingQRCode = false;
    private transient ItemStack qrMapReplacedItem = null;

    public GoogleAuthenticator getGoogleAuthenticator() {
        if (googleAuthenticator == null) {
            googleAuthenticator = new GoogleAuthenticator();
        }
        return googleAuthenticator;
    }

    public boolean isCorrect(Integer code) {
        return getGoogleAuthenticator().authorize(secret, code);
    }

    public boolean isProtected() {
        return secret != null;
    }

    public GoogleAuthenticatorKey updateKey() {
        GoogleAuthenticatorKey key = getGoogleAuthenticator().createCredentials();
        setSecret(key.getKey());
        return key;
    }

    public String createQRCode(String username) {
        if (!isProtected())
            return null;
        return String.format(googleFormat, username, ConfigReader.getServerIP(), secret);
    }

    public boolean protectPlayer(Player player) {
        if (isProtected()) {
            return false;
        }
        fancyMessage = null;
        updateKey();
        StringManager.sendMessage(player, "&cHello &d%s &cyour new authentication key has been created:\n&b&l%s", player.getName(), getSecret());
        if(ConfigReader.useMapQR()){
            startViewingQRMap(player);
        } else {
            this.sendFancyQRMessage(player);
        }
        this.setFirstTime(true);
        return true;
    }

    public void sendFancyQRMessage(Player player) {
        if (fancyMessage == null) {
            fancyMessage = new FancyMessage()
                    .text("Click here")
                    .color(ChatColor.GREEN)
                    .style(ChatColor.UNDERLINE)
                    .link(createQRCode(player.getName()))
                    .then()
                    .text(" for the ")
                    .color(ChatColor.YELLOW)
                    .then()
                    .text("QR")
                    .color(ChatColor.YELLOW)
                    .style(ChatColor.BOLD)
                    .then()
                    .text(" code!")
                    .color(ChatColor.YELLOW);
        }
        fancyMessage.send(player);
    }

    public void startViewingQRMap(Player p){
        if(isViewingQRCode()) return;

        ImageMapRenderer mapRenderer = null;
        try {
            mapRenderer = new ImageMapRenderer(this, p.getName());
        } catch (WriterException e) {
            e.printStackTrace();
            p.sendMessage(ChatColor.RED+"There was an error rendering your 2FA QR code!");
            return;
        }

        qrMapReplacedItem = p.getItemInHand();
        isViewingQRCode = true;

        ItemStack itemStack = new ItemStack(Material.MAP);
        MapView map = Bukkit.createMap(p.getWorld());
        itemStack.setDurability(map.getId());
        p.setItemInHand(itemStack);

        Location playerLocation = p.getLocation();
        playerLocation.setPitch(90);
        p.teleport(playerLocation);

        map.getRenderers().forEach(map::removeRenderer);
        map.addRenderer(mapRenderer);
        p.sendMap(map);
    }

    public void stopViewingQRMap(Player p){
        if(!isViewingQRCode()) return;
        p.setItemInHand(qrMapReplacedItem);
        qrMapReplacedItem = null;
        isViewingQRCode = false;
    }

    public BitMatrix generateQRCode(String username) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(String.format(encodeFormat, username, ConfigReader.getServerIP(), secret), BarcodeFormat.QR_CODE, 128, 128);
    }

}
