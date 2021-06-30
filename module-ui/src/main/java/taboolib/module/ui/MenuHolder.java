package taboolib.module.ui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @author 坏黑
 * @since 2019-05-21 20:28
 */
public class MenuHolder implements InventoryHolder {

    private final MenuBuilder builder;

    public MenuHolder(MenuBuilder builder) {
        this.builder = builder;
    }

    public MenuBuilder getBuilder() {
        return builder;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public static MenuBuilder get(Inventory inventory) {
        return inventory.getHolder() instanceof MenuHolder ? ((MenuHolder) inventory.getHolder()).getBuilder() : null;
    }
}
