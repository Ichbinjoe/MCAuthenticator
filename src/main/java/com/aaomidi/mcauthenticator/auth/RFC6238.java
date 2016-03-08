package com.aaomidi.mcauthenticator.auth;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.map.ImageMapRenderer;
import com.aaomidi.mcauthenticator.model.User;
import com.google.zxing.WriterException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 * @since 1.1
 */
public class RFC6238 implements Authenticator {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private static final transient String googleFormat =
            "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=" +
                    "otpauth://totp/%s@%s%%3Fsecret%%3D%s";
    private final String serverIp;
    private final Map<User, String> temporarySecrets = new HashMap<>();
    private final MCAuthenticator mcAuthenticator;

    public RFC6238(String serverIp, MCAuthenticator mcAuthenticator) {
        this.serverIp = serverIp;
        this.mcAuthenticator = mcAuthenticator;
        validateServerTime();
    }

    @Override
    public boolean authenticate(User u, String input) {
        Integer code;
        try {
            code = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }

        String authSecret = temporarySecrets.get(u);
        boolean temp = authSecret != null;
        if (!temp) {
            if (u.getUserData() == null || u.getUserData().getAuthType() != 0)
                return false; //Isn't 2fa.
            authSecret = u.getUserData().getSecret();
        }
        boolean result = gAuth.authorize(authSecret, code);
        if (temp && result) {
            temporarySecrets.remove(u);
            u.setUserInfo(authSecret, 0);
        }

        return result;
    }

    @Override
    public boolean isFormat(String s) {
        return s.matches("^\\d{6}$");
    }

    @Override
    public void initUser(User u, Player p) {
        String newKey = createNewKey();

        ImageMapRenderer mapRenderer;
        try {
            mapRenderer = new ImageMapRenderer(p.getName(), newKey, serverIp);
        } catch (WriterException e) {
            mcAuthenticator.getC().sendDirect(p,
                    "&cThere was an error rendering your 2FA QR code!");
            mcAuthenticator.handleException(e);
            return;
        }

        temporarySecrets.put(u, newKey);
        if (!u.isInventoryStored())
            u.storeInventory(p);

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

        String msg = mcAuthenticator.getC().message("sendAuthCode");
        msg = msg.replaceAll("%code%", newKey);
        msg = msg.replaceAll("%url%", getQRUrl(p.getName(), newKey));
        mcAuthenticator.getC().send(p, msg);
    }

    @Override
    public void quitUser(User u, Player p) {
        temporarySecrets.remove(u);
        u.reverseInventory(p);
    }

    private String getQRUrl(String username, String secret) {
        if (secret == null)
            return null;
        return String.format(googleFormat, username, serverIp, secret);
    }

    private String createNewKey() {
        return gAuth.createCredentials().getKey();
    }

    private void validateServerTime() {
        // Since 1.0.2
        try {
            String TIME_SERVER = "http://icanhazepoch.com";
            HttpURLConnection timeCheckQuery =
                    (HttpURLConnection) new URL(TIME_SERVER).openConnection();
            timeCheckQuery.setReadTimeout(4000);
            timeCheckQuery.setConnectTimeout(4000);
            timeCheckQuery.connect();
            int responseCode = timeCheckQuery.getResponseCode();
            if (responseCode != 200) {
                mcAuthenticator.getLogger().info("Could not validate the server's time! Ensure" +
                        " that the server's time is within specification!");
                return;
            }
            byte[] response = new byte[1024]; // Response should never be over 1kB
            InputStream inputStream = timeCheckQuery.getInputStream();
            int len = inputStream.read(response);
            String rsp = new String(response, 0, len, Charset.defaultCharset()).trim();
            Long unixSeconds = Long.parseLong(rsp);
            long myUnixSeconds = (System.currentTimeMillis() / 1000);
            int diff = (int) (unixSeconds - myUnixSeconds);
            if (Math.abs(diff) > 30) {
                mcAuthenticator.getLogger().severe("Your server's Unix time is off by "
                        + Math.abs(diff) + " seconds! 2FA may not work! Please "
                        + "correct this to make sure 2FA works.");
            }
        } catch (IOException | NumberFormatException e) {
            mcAuthenticator.getLogger().log(Level.WARNING, "Was not able to validate the server's" +
                    " Unix time against an external service: Please ensure your" +
                    " server's time is set correctly or 2FA may not operate right.", e);
        }
    }
}
