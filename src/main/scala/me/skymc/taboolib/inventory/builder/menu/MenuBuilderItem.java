package me.skymc.taboolib.inventory.builder.menu;

import org.bukkit.inventory.ItemStack;

/**
 * @Author sky
 * @Since 2018-08-22 15:36
 */
public class MenuBuilderItem {

    private final ItemStack itemStack;
    private final MenuBuilderCallable callable;

    public MenuBuilderItem(ItemStack itemStack, MenuBuilderCallable callable) {
        this.itemStack = itemStack;
        this.callable = callable;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public MenuBuilderCallable getCallable() {
        return callable;
    }
}
