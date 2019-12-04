package io.izzel.taboolib.util.item.inventory;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-21 18:16
 */
@TListener
class ClickListener implements Listener {

    @EventHandler
    public void e(PluginDisableEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && e.getPlugin().equals(((MenuHolder) player.getOpenInventory().getTopInventory().getHolder()).getBuilder().getPlugin())).forEach(HumanEntity::closeInventory);
    }

    @EventHandler
    public void e(InventoryOpenEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            Bukkit.getScheduler().runTask(TabooLib.getPlugin(), () -> ((MenuHolder) e.getInventory().getHolder()).getBuilder().getBuildTask().run(e.getInventory()));
            Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> ((MenuHolder) e.getInventory().getHolder()).getBuilder().getBuildTaskAsync().run(e.getInventory()));
        }
    }

    @EventHandler
    public void e(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            // lock hand
            if (((MenuHolder) e.getInventory().getHolder()).getBuilder().isLockHand() && (e.getRawSlot() - e.getInventory().getSize() - 27 == e.getWhoClicked().getInventory().getHeldItemSlot() || (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.getHotbarButton() == e.getWhoClicked().getInventory().getHeldItemSlot()))) {
                e.setCancelled(true);
            }
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getClickTask()).ifPresent(t -> t.run(new ClickEvent(ClickType.CLICK, e, ((MenuHolder) e.getInventory().getHolder()).getBuilder().getSlot(e.getRawSlot()))));
            // drop on empty area
            if (!e.isCancelled() && Items.nonNull(e.getCurrentItem()) && e.getClick() == org.bukkit.event.inventory.ClickType.DROP) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCurrentItem());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(TabooLib.getPlugin(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.setCurrentItem(null);
                }
            }
            // drop by keyboard
            else if (!e.isCancelled() && Items.nonNull(e.getCursor()) && e.getRawSlot() == -999) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCursor());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(TabooLib.getPlugin(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.getView().setCursor(null);
                }
            }
        }
    }

    @EventHandler
    public void e(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getClickTask()).ifPresent(t -> t.run(new ClickEvent(ClickType.DRAG, e, ' ')));
        }
    }

    @EventHandler
    public void e(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            Optional.ofNullable(((MenuHolder) e.getInventory().getHolder()).getBuilder().getCloseTask()).ifPresent(t -> t.run(e));
        }
    }

    @EventHandler
    public void e(PlayerDropItemEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand() && !e.getItemDrop().hasMetadata("internal-drop")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void e(PlayerItemHeldEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }
}
