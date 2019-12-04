package io.izzel.taboolib.util.item.inventory.stored;

import io.izzel.taboolib.util.item.ItemStacker;
import io.izzel.taboolib.util.item.inventory.ClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @Author sky
 * @Since 2019-12-03 19:20
 */
public class ActionQuickTake extends Action {

    @Override
    public ItemStack getCurrent(ClickEvent e) {
        return e.getClicker().getItemOnCursor();
    }

    @Override
    public void setCurrent(ClickEvent e, ItemStack item) {
        if (item != null) {
            ItemStacker.moveItemFromChest(item, e.getClicker());
        }
        e.getClicker().setItemOnCursor(null);
    }

    @Override
    public int getCurrentSlot(ClickEvent e) {
        return e.getRawSlot();
    }
}
