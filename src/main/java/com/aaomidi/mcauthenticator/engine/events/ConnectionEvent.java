package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.auth.Authenticator;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.model.UserData;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(final PlayerJoinEvent event) {
        try {
            instance.handlePlayer(event.getPlayer(), userDataCache.remove(event.getPlayer().getUniqueId()));
        } catch (IOException | SQLException e) {
            instance.handleException(e);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        User leave = instance.getCache().leave(e.getPlayer().getUniqueId());
        if (leave != null) {
            for (Authenticator a : instance.getAuthenticators()) {
                a.quitUser(leave, e.getPlayer());
            }
        }
    }
}
