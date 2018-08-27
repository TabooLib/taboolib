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

    public MenuBuilderHolder(boolean lock, HashMap<Integer, MenuBuilderItem> items) {
        this.lock = lock;
        this.items = items;
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
}