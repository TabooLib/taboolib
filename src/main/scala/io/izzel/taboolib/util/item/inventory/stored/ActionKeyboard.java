package io.izzel.taboolib.util.item.inventory.stored;

import io.izzel.taboolib.util.item.inventory.ClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @Author sky
 * @Since 2019-12-03 19:20
 */
public class ActionKeyboard extends Action {

    @Override
    public ItemStack getCurrent(ClickEvent e) {
        return e.getClicker().getInventory().getItem(e.castClick().getHotbarButton());
    }

    @Override
    public void setCurrent(ClickEvent e, ItemStack item) {
        e.getClicker().getInventory().setItem(e.castClick().getHotbarButton(), item);
    }

    @Override
    public int getCurrentSlot(ClickEvent e) {
        return e.getRawSlot();
    }
}
