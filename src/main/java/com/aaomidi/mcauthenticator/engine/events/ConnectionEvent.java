package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.config.ConfigReader;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.util.StringManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public class ConnectionEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        StringManager.log("&dIs staff member:" + player.hasPermission(ConfigReader.getStaffPermission()) + "");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(PlayerJoinEvent event) {
        this.instance.handlePlayer(event.getPlayer());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = instance.getDataManager().getDataFile().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }
        if (user.getInetAddress() != null && user.getInetAddress().equals(player.getAddress().getAddress())) {
            StringManager.sendMessage(player, "&bYou were automatically authenticated since your IP has not changed.");
            user.setAuthenticated(true);
            return;
        }
        if (!user.isProtected()) {
            if (!user.protectPlayer(player)) {
                StringManager.sendMessage(player, "&cSevere error occurred when protecting you!");
            }
        }
        StringManager.sendMessage(player, "&dPlease enter your authentication code using Google Authenticator: ");
        instance.getDataManager().saveFile();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        User user = instance.getDataManager().getDataFile().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        if(!user.isAuthenticated() && user.isFirstTime()){
            user.setSecret(null);
        }
    }
}
