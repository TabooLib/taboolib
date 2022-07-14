package io.izzel.taboolib.util.item.inventory;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * @author 坏黑
 * @since 2019-05-21 18:14
 */
public interface CloseTask {

    void run(InventoryCloseEvent event);

}
