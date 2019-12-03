package io.izzel.taboolib.util.item.inventory;

import com.google.common.collect.Lists;
import io.izzel.taboolib.util.lite.Servers;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-05-21 18:09
 */
public class ClickEvent {

    private ClickType clickType;
    private Event event;
    private char slot;

    public ClickEvent(ClickType clickType, Event event, char slot) {
        this.clickType = clickType;
        this.event = event;
        this.slot = slot;
    }

    public List<ItemStack> getAffectItems() {
        return clickType == ClickType.CLICK ? Servers.getAffectItemInClickEvent((InventoryClickEvent) event) : Lists.newArrayList();
    }

    public InventoryClickEvent castClick() {
        return (InventoryClickEvent) event;
    }

    public InventoryDragEvent castDrag() {
        return (InventoryDragEvent) event;
    }

    public int getRawSlot() {
        return clickType == ClickType.CLICK ? castClick().getRawSlot() : -1;
    }

    public char getSlot() {
        return slot;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Player getClicker() {
        return (Player) ((InventoryInteractEvent) event).getWhoClicked();
    }

    public Inventory getInventory() {
        return ((InventoryEvent) event).getInventory();
    }

    public void setCancelled(boolean c) {
        ((Cancellable) event).setCancelled(true);
    }

    public boolean isCancelled() {
        return ((Cancellable) event).isCancelled();
    }

    public ItemStack getCurrentItem() {
        return clickType == ClickType.CLICK ? castClick().getCurrentItem() : null;
    }

    public void setCurrentItem(ItemStack item) {
        if (clickType == ClickType.CLICK) {
            castClick().setCurrentItem(item);
        }
    }
}
