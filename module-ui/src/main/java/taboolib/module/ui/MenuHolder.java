package taboolib.module.ui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import taboolib.module.ui.type.Basic;

/**
 * @author 坏黑
 * @since 2019-05-21 20:28
 */
public class MenuHolder implements InventoryHolder {

    private final Basic builder;

    public MenuHolder(Basic builder) {
        this.builder = builder;
    }

    public Basic getBuilder() {
        return builder;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public static Basic get(Inventory inventory) {
        return inventory.getHolder() instanceof MenuHolder ? ((MenuHolder) inventory.getHolder()).getBuilder() : null;
    }
}
