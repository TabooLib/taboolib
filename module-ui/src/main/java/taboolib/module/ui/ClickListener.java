package taboolib.module.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.metadata.FixedMetadataValue;
import taboolib.common.platform.*;
import taboolib.platform.BukkitPlugin;

@PlatformSide(Platform.BUKKIT)
public class ClickListener {

    public static final ClickListener INSTANCE = new ClickListener();

    @SubscribeEvent
    public void e(PluginDisableEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(player -> MenuHolder.get(player.getOpenInventory().getTopInventory()) != null).forEach(HumanEntity::closeInventory);
    }

    @SubscribeEvent
    public void e(InventoryOpenEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            FunctionKt.submit(false, false, 1, 0, platformTask -> {
                builder.getBuildTask().run(e.getInventory());
                return null;
            });
            FunctionKt.submit(false, true, 1, 0, platformTask -> {
                builder.getBuildTaskAsync().run(e.getInventory());
                return null;
            });
        }
    }

    @SubscribeEvent
    public void e(InventoryClickEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            // lock hand
            if (builder.isLockHand() && (e.getRawSlot() - e.getInventory().getSize() - 27 == e.getWhoClicked().getInventory().getHeldItemSlot() || (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.getHotbarButton() == e.getWhoClicked().getInventory().getHeldItemSlot()))) {
                e.setCancelled(true);
            }
            try {
                builder.getClickTask().run(new ClickEvent(ClickType.CLICK, e, builder.getSlot(e.getRawSlot())));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            // drop on empty area
            if (!e.isCancelled() && ItemUtils.nonNull(e.getCurrentItem()) && e.getClick() == org.bukkit.event.inventory.ClickType.DROP) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCurrentItem());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(BukkitPlugin.getInstance(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.setCurrentItem(null);
                }
            }
            // drop by keyboard
            else if (!e.isCancelled() && ItemUtils.nonNull(e.getCursor()) && e.getRawSlot() == -999) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCursor());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(BukkitPlugin.getInstance(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.getView().setCursor(null);
                }
            }
        }
    }

    @SubscribeEvent
    public void e(InventoryDragEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            try {
                builder.getClickTask().run(new ClickEvent(ClickType.DRAG, e, ' '));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void e(InventoryCloseEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            try {
                builder.getCloseTask().run(e);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void e(PlayerDropItemEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand() && !e.getItemDrop().hasMetadata("internal-drop")) {
            e.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void e(PlayerItemHeldEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }

    @SubscribeEvent(bind = "org.bukkit.event.player.PlayerSwapHandItemsEvent")
    public void onSwap(OptionalEvent ope) {
        PlayerSwapHandItemsEvent e = ope.cast(PlayerSwapHandItemsEvent.class);
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }
}
