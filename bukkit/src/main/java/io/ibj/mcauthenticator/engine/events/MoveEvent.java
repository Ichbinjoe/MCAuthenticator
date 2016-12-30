package io.ibj.mcauthenticator.engine.events;

import io.ibj.mcauthenticator.MCAuthenticator;
import io.ibj.mcauthenticator.model.User;
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
public final class MoveEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        User u = instance.getCache().get(player.getUniqueId());

        if (u != null && u.authenticated()) return;

        event.setTo(from);
    }
}
