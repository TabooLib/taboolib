package io.izzel.taboolib.module.item.inventory;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * @Author 坏黑
 * @Since 2019-05-21 18:14
 */
public interface CloseTask {

    void run(InventoryCloseEvent event);

}
