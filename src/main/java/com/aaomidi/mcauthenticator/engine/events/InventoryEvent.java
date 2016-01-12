package com.aaomidi.mcauthenticator.engine.events;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Joseph Hirschfeld
 * @date 1/11/2016
 */
public class InventoryEvent implements Listener {

    private final MCAuthenticator instance;

    public InventoryEvent(MCAuthenticator instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (isInQR((Player) e.getWhoClicked())) {
            e.setResult(Event.Result.DENY);
            e.getWhoClicked().closeInventory();//Should be viewing map
        }
    }

    @EventHandler
    public void onInventoryChange(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (isInQR((Player) e.getWhoClicked())) {
            e.setResult(Event.Result.DENY);
            e.getWhoClicked().closeInventory();//Should be viewing map
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if(isInQR(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        User user = instance.getDataManager().getDataFile().getUser(e.getPlayer().getUniqueId());
        if(user != null && user.isViewingQRCode()){
            user.stopViewingQRMap(e.getPlayer());
        }
    }

    public boolean isInQR(Player p) {
        User user = instance.getDataManager().getDataFile().getUser(p.getUniqueId());
        return user != null && user.isViewingQRCode();
    }

}
