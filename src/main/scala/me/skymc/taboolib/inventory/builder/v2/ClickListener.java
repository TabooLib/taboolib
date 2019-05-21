package me.skymc.taboolib.inventory.builder.v2;

import me.skymc.taboolib.listener.TListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-21 18:16
 */
@TListener
class ClickListener implements Listener {

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && e.getPlugin().equals(((MenuHolder) player.getOpenInventory().getTopInventory().getHolder()).getBuilder().getPlugin())).forEach(HumanEntity::closeInventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            if (((MenuHolder) e.getInventory().getHolder()).getBuilder().isLockHand() && (e.getRawSlot() - e.getInventory().getSize() - 27 == e.getWhoClicked().getInventory().getHeldItemSlot() || (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.getHotbarButton() == e.getWhoClicked().getInventory().getHeldItemSlot()))) {
                e.setCancelled(true);
            }
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getClickTask()).ifPresent(t -> t.run(new ClickEvent(ClickType.CLICK, e)));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getClickTask()).ifPresent(t -> t.run(new ClickEvent(ClickType.DRAG, e)));
        }
    }

    @EventHandler
    public void onDrag(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getCloseTask()).ifPresent(t -> t.run(e));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }
}
