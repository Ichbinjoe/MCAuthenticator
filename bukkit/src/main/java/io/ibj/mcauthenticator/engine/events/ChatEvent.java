package io.ibj.mcauthenticator.engine.events;

import io.ibj.mcauthenticator.MCAuthenticator;
import io.ibj.mcauthenticator.model.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Iterator;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public final class ChatEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = instance.getCache().get(player.getUniqueId());

        if (user == null) {
            event.getRecipients().clear();
            return;
        }

        if (user.authenticated()) {
            Iterator<Player> recipients = event.getRecipients().iterator();
            while(recipients.hasNext()) {
                User u = instance.getCache().get(recipients.next().getUniqueId());
                if(u != null && !u.authenticated()) recipients.remove();
            }
            return;
        }

        if (instance.getC().getBungeePluginChannel() == null) {
            boolean authenticate = false;
            try {
                authenticate = user.authenticate(event.getMessage(), player);
            } catch (Exception e) {
                instance.getC().sendDirect(player, "&cThere was a fatal exception when trying to authenticate you!");
            }

            if (authenticate) {
                instance.getC().send(player, instance.getC().message("authenticated"));
            } else {
                instance.getC().send(player, instance.getC().message("authFailed"));
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = instance.getCache().get(player.getUniqueId());

        if (user != null && user.authenticated()) return;
        instance.getC().send(player, instance.getC().message("notAuthed"));
        event.setCancelled(true);
    }
}
