package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public class MoveEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        Player player = event.getPlayer();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ() && !instance.getC().isMapBasedQR()) {
            return;
        }

        User user = instance.getDataManager().getDataFile().getUser(player.getUniqueId());
        if (user == null || user.isAuthenticated()) {
            return;
        }

        if (user.isFirstTime()) {
            // StringManager.sendMessageWithoutColor(player, ChatColor.RED + "Visit the following link to see the QR code:\n" + ChatColor.AQUA + user.createQRCode(player.getName()));
            if(instance.getC().isMapBasedQR() && user.isViewingQRCode()){
                player.sendMessage(ChatColor.RED+"Right click your map to view your QR code! Once you have done this, enter your latest code.");
            } else {
                user.sendFancyQRMessage(player);
            }
        }

        event.setTo(from);
    }
}
