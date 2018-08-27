package me.skymc.taboolib.inventory.builder.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @Author sky
 * @Since 2018-08-22 15:44
 */
public class MenuBuilderEvent {

    private final InventoryClickEvent parentEvent;
    private final Player player;
    private final ItemStack clickItem;
    private final int clickSlot;

    public MenuBuilderEvent(InventoryClickEvent parentEvent, Player player, ItemStack clickItem, int clickSlot) {
        this.parentEvent = parentEvent;
        this.player = player;
        this.clickItem = clickItem;
        this.clickSlot = clickSlot;
    }

    public InventoryClickEvent getParentEvent() {
        return parentEvent;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getClickItem() {
        return clickItem;
    }

    public int getClickSlot() {
        return clickSlot;
    }

    public void setCancelled(boolean canceled) {
        parentEvent.setCancelled(canceled);
    }

    public boolean isCancelled() {
        return parentEvent.isCancelled();
    }
}
