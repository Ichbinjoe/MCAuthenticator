package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.util.StringManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.logging.Level;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public class ChatEvent implements Listener {
    private final MCAuthenticator instance;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = instance.getDataManager().getDataFile().getUser(player.getUniqueId());

        if (user == null || user.isAuthenticated()) {
            return;
        }

        String message = event.getMessage();
        int code;
        try {
            code = Integer.valueOf(message);

        } catch (NumberFormatException ex) {
            code = -1;
        }
        StringManager.log(Level.SEVERE, code + "");

        boolean result = user.isCorrect(code);
        if (result) {
            StringManager.sendMessage(player, "&bYou have been authenticated.");
            user.setAuthenticated(true);
            if(user.isViewingQRCode())
                user.stopViewingQRMap(player);
            user.setInetAddress(player.getAddress().getAddress());
            instance.getDataManager().saveFile();
        } else {
            StringManager.sendMessage(player, "&cIncorrect password.");
        }

        event.getRecipients().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = instance.getDataManager().getDataFile().getUser(player.getUniqueId());

        if (user == null || user.isAuthenticated()) {
            return;
        }
        StringManager.sendMessage(player, "&cYou can not enter any commands until you authenticate.");
        event.setCancelled(true);
    }
}
