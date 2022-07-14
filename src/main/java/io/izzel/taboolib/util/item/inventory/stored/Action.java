package io.izzel.taboolib.util.item.inventory.stored;

import io.izzel.taboolib.util.item.inventory.ClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Action {

    public abstract ItemStack getCurrent(ClickEvent e);

    public abstract void setCurrent(ClickEvent e, ItemStack item);

    public abstract int getCurrentSlot(ClickEvent e);
}
