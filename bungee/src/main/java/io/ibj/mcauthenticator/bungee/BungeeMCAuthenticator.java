package io.ibj.mcauthenticator.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/2/16
 */
public class BungeeMCAuthenticator extends Plugin implements Listener {

    private final Set<UUID> authenticatedUsers = new HashSet<>();

    private String pluginMessageChannel;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        File config = new File(getDataFolder(), "config.yml");

        if (!config.exists()) {
            try {
                config.createNewFile();

                try (InputStream bungeeConfig = getResourceAsStream("bungeeconfig.yml")) {
                    try (OutputStream configStream = new FileOutputStream(config)) {
                        byte[] buf = new byte[1024];
                        int read;
                        while ((read = bungeeConfig.read(buf)) > 0)
                            configStream.write(buf, 0, read);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create config file!", e);
            }
        }

        // Load the configuration.
        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
            pluginMessageChannel = configuration.getString("pluginChannel", "MCAuthenticator");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load configuration", e);
        }

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(pluginMessageChannel);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!e.getTag().equals(pluginMessageChannel)) return;

        // Do not accept plugin messages from non-servers
        if (!(e.getSender() instanceof Server)) {
            e.setCancelled(true);
            return;
        }

        if (e.getData().length != 1) return;

        if (e.getData()[0] == 0x00)
            authenticatedUsers.add(((ProxiedPlayer) e.getReceiver()).getUniqueId());
        else if (e.getData()[0] == 0x01)
            authenticatedUsers.remove(((ProxiedPlayer) e.getReceiver()).getUniqueId());
        else if (e.getData()[0] == 0x02) {
            if (authenticatedUsers.contains(((ProxiedPlayer) e.getReceiver()).getUniqueId()))
                ((ProxiedPlayer) e.getReceiver()).getServer().sendData(pluginMessageChannel, new byte[]{0x00});
            else
                ((ProxiedPlayer) e.getReceiver()).getServer().sendData(pluginMessageChannel, new byte[]{0x01});
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        if (!authenticatedUsers.contains(((ProxiedPlayer) e.getSender()).getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!authenticatedUsers.contains(((ProxiedPlayer) e.getSender()).getUniqueId())) {
            byte[] msgBuffer = e.getMessage().getBytes(StandardCharsets.UTF_8);
            byte[] plMsgPayload = new byte[msgBuffer.length + 1];
            plMsgPayload[0] = 0x02;
            System.arraycopy(msgBuffer, 0, plMsgPayload, 1, msgBuffer.length);
            ((ProxiedPlayer) e.getSender()).getServer().sendData(pluginMessageChannel, plMsgPayload);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        authenticatedUsers.remove(e.getPlayer().getUniqueId());
    }
}
