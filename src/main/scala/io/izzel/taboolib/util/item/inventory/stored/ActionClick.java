package io.izzel.taboolib.util.item.inventory.stored;

import io.izzel.taboolib.util.item.inventory.ClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author sky
 * @since 2019-12-03 19:17
 */
public class ActionClick extends Action{

    @Override
    public ItemStack getCurrent(ClickEvent e) {
        return e.getClicker().getItemOnCursor();
    }

    @Override
    public void setCurrent(ClickEvent e, ItemStack item) {
        e.getClicker().setItemOnCursor(item);
    }

    @Override
    public int getCurrentSlot(ClickEvent e) {
        return e.getRawSlot();
    }
}
