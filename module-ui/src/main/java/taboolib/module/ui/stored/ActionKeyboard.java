package taboolib.module.ui.stored;

import taboolib.common.Isolated;
import taboolib.module.ui.ClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author sky
 * @since 2019-12-03 19:20
 */
@Isolated
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
