package io.ibj.mcauthenticator.engine.events;

import io.ibj.mcauthenticator.MCAuthenticator;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * @author Joseph Hirschfeld
 * @date 1/11/2016
 */
public final class InventoryEvent implements Listener {

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
    public void onPlayerMoveItemHand(PlayerItemHeldEvent e){
        if(!auth(e)) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if(!auth(e)) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
        if(!auth(e)) e.setCancelled(true);
    }

    private boolean auth(PlayerEvent e) {
        return instance.getCache().get(e.getPlayer().getUniqueId()).authenticated();
    }

    public boolean isInQR(Player p) {
        return instance.getCache().get(p.getUniqueId()).isInit();
    }

}
