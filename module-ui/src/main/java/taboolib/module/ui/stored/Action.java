package taboolib.module.ui.stored;

import taboolib.common.Isolated;
import taboolib.module.ui.ClickEvent;
import org.bukkit.inventory.ItemStack;

@Isolated
public abstract class Action {

    public abstract ItemStack getCurrent(ClickEvent e);

    public abstract void setCurrent(ClickEvent e, ItemStack item);

    public abstract int getCurrentSlot(ClickEvent e);
}
