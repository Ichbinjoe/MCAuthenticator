package io.ibj.mcauthenticator.engine.events;

import io.ibj.mcauthenticator.MCAuthenticator;
import io.ibj.mcauthenticator.auth.Authenticator;
import io.ibj.mcauthenticator.model.User;
import io.ibj.mcauthenticator.model.UserData;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public final class ConnectionEvent implements Listener {
    private final MCAuthenticator instance;

    private final Map<UUID, UserData> userDataCache = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConnect(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        try {
            UserData d = instance.getDataSource().getUser(e.getUniqueId());
            if (d == null) return;
            userDataCache.put(e.getUniqueId(), d);
        } catch (IOException | SQLException e1) {
            instance.handleException(e1);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnect(final PlayerJoinEvent event) {
        try {
            instance.handlePlayer(event.getPlayer(), userDataCache.remove(event.getPlayer().getUniqueId()));
        } catch (IOException | SQLException e) {
            instance.handleException(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        User leave = instance.getCache().leave(e.getPlayer().getUniqueId());
        if (leave != null) {
            for (Authenticator a : instance.getAuthenticators()) {
                a.quitUser(leave, e.getPlayer());
            }
        }
    }
}
