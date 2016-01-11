package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.config.ConfigReader;
import com.aaomidi.mcauthenticator.util.StringManager;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
    private transient String format = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
    private transient GoogleAuthenticator googleAuthenticator;
    @Getter
    @Setter
    private transient boolean authenticated = false;
    private transient FancyMessage fancyMessage = null;

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
        return String.format(format, username, ConfigReader.getServerIP(), secret);
    }

    public boolean protectPlayer(Player player) {
        if (isProtected()) {
            return false;
        }
        fancyMessage = null;
        updateKey();
        StringManager.sendMessage(player, "&cHello &d%s &cyour new authentication key has been created:\n&b&l%s", player.getName(), getSecret());
        this.sendFancyQRMessage(player);
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

}
