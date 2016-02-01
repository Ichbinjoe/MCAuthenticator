package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import lombok.RequiredArgsConstructor;
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
        User u = instance.getCache().get(player.getUniqueId());

        if(u.isViewingQRCode()) {
            event.setTo(from);
            return;
        }

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        if (u.authenticated()) return;

        if (u.isFirstTime()) {
            if(!u.isViewingQRCode()) {
                u.sendFancyQRMessage(player);
            }
        }

        event.setTo(from);
    }
}
