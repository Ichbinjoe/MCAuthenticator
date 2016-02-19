package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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

        if (user.authenticated()) {
            return;
        }

        boolean authenticate = user.authenticate(event.getMessage(), player);

        if(authenticate){
            instance.getC().send(player, instance.getC().message("authenticated"));
        } else {
            instance.getC().send(player, instance.getC().message("authFailed"));
        }

        event.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = instance.getCache().get(player.getUniqueId());

        if (user.authenticated()) return;
        instance.getC().send(player, instance.getC().message("notAuthed"));
        event.setCancelled(true);
    }
}
