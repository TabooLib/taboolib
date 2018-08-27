package me.skymc.taboolib.inventory.builder;

import me.skymc.taboolib.inventory.builder.menu.MenuBuilderCallable;
import me.skymc.taboolib.inventory.builder.menu.MenuBuilderHolder;
import me.skymc.taboolib.inventory.builder.menu.MenuBuilderItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @Author sky
 * @Since 2018-08-22 13:40
 * @BuilderVersion 1.0
 */
public class MenuBuilder {

    private boolean lock;
    private String name;
    private int rows = 9;

    private HashMap<Integer, MenuBuilderItem> items = new HashMap<>();

    public MenuBuilder() {
        this(true);
    }

    public MenuBuilder(boolean lock) {
        this.lock = lock;
    }

    public MenuBuilder lock(boolean lock) {
        this.lock = lock;
        return this;
    }

    public MenuBuilder name(String name) {
        this.name = name;
        return this;
    }

    public MenuBuilder rows(int rows) {
        this.rows = rows * 9;
        return this;
    }

    public MenuBuilder item(ItemStack itemStack, int... slots) {
        Arrays.stream(slots).forEach(slot -> items.put(slot, new MenuBuilderItem(itemStack, null)));
        return this;
    }

    public MenuBuilder item(ItemStack itemStack, MenuBuilderCallable callable, int... slots) {
        Arrays.stream(slots).forEach(slot -> items.put(slot, new MenuBuilderItem(itemStack, callable)));
        return this;
    }

    public Inventory build() {
        Inventory inventory = Bukkit.createInventory(new MenuBuilderHolder(lock, items), rows, name);
        items.forEach((key, value) -> inventory.setItem(key, value.getItemStack()));
        return inventory;
    }
}
