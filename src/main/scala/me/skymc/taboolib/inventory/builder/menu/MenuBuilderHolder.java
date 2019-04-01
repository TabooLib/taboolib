package me.skymc.taboolib.inventory.builder.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

/**
 * @author sky
 * @Since 2018-08-22 13:40
 */
public class MenuBuilderHolder implements InventoryHolder {

    private final boolean lock;
    private final HashMap<Integer, MenuBuilderItem> items;
    private final Inventory parent;

    public MenuBuilderHolder(boolean lock, HashMap<Integer, MenuBuilderItem> items, Inventory parent) {
        this.lock = lock;
        this.items = items;
        this.parent = parent;
    }

    public MenuBuilderHolder(boolean lock, HashMap<Integer, MenuBuilderItem> items) {
        this(lock, items, null);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public boolean isLock() {
        return lock;
    }

    public HashMap<Integer, MenuBuilderItem> getItems() {
        return items;
    }

    public Inventory getParent() {
        return parent;
    }
}