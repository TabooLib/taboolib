package me.skymc.taboolib.inventory.builder.menu;

import me.skymc.taboolib.listener.TListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-08-22 13:40
 */
@TListener
public class MenuBuilderListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuBuilderHolder)) {
            return;
        }
        MenuBuilderHolder holder = (MenuBuilderHolder) e.getInventory().getHolder();
        if (holder.isLock() || e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            e.setCancelled(true);
        }
        Optional.ofNullable(holder.getItems().get(e.getRawSlot())).ifPresent(item -> {
            if (item.getCallable() != null) {
                item.getCallable().call(new MenuBuilderEvent(e, (Player) e.getWhoClicked(), e.getCurrentItem(), e.getRawSlot()));
            }
        });
    }

}
