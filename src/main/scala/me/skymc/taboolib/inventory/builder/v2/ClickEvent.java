package me.skymc.taboolib.inventory.builder.v2;

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

    public ClickEvent(ClickType clickType, Event event) {
        this.clickType = clickType;
        this.event = event;
    }

    public InventoryClickEvent castClick() {
        return (InventoryClickEvent) event;
    }

    public InventoryDragEvent castDrag() {
        return (InventoryDragEvent) event;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Player getClicker() {
        return (Player) ((InventoryInteractEvent) event).getWhoClicked();
    }
}
