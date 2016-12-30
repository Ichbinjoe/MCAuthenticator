package io.ibj.mcauthenticator.engine.events;

import io.ibj.mcauthenticator.MCAuthenticator;
import io.ibj.mcauthenticator.model.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class InteractEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User u = instance.getCache().get(player.getUniqueId());

        if (u != null && u.authenticated()) return;

        event.setCancelled(true);
    }
}
