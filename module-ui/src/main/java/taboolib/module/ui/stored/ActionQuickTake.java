package taboolib.module.ui.stored;

import taboolib.common.Isolated;
import taboolib.module.ui.ClickEvent;
import org.bukkit.inventory.ItemStack;
import taboolib.module.ui.ItemStacker;

/**
 * @author sky
 * @since 2019-12-03 19:20
 */
@Isolated
public class ActionQuickTake extends Action {

    @Override
    public ItemStack getCurrent(ClickEvent e) {
        return e.getClicker().getItemOnCursor();
    }

    @Override
    public void setCurrent(ClickEvent e, ItemStack item) {
        if (item != null) {
            ItemStacker.MINECRAFT.moveItemFromChest(item, e.getClicker());
        }
        e.getClicker().setItemOnCursor(null);
    }

    @Override
    public int getCurrentSlot(ClickEvent e) {
        return e.getRawSlot();
    }
}
