package io.izzel.taboolib.util.item.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;

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

    public InventoryClickEvent castClick() {
        return (InventoryClickEvent) event;
    }

    public InventoryDragEvent castDrag() {
        return (InventoryDragEvent) event;
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
}
