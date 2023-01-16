package taboolib.module.ui.virtual

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.type.Basic
import taboolib.platform.util.broadcast

/**
 * 将背包转换为 VirtualInventory 实例
 */
fun Inventory.virtualize(storageContents: List<ItemStack>? = null): VirtualInventory {
    return VirtualInventory(this, storageContents)
}

/**
 * 使玩家打开虚拟页面
 */
fun HumanEntity.openVirtualInventory(inventory: VirtualInventory): RemoteInventory {
    val remoteInventory = InventoryHandler.instance.openInventory(this as Player, inventory, ItemStack(Material.AIR))
    inventory.remoteInventory = remoteInventory
    InventoryHandler.playerRemoteInventoryMap[name] = remoteInventory
    // 唤起事件
    if (isPrimaryThread) {
        Bukkit.getPluginManager().callEvent(InventoryOpenEvent(VirtualInventoryView(remoteInventory)))
    } else {
        submit { Bukkit.getPluginManager().callEvent(InventoryOpenEvent(VirtualInventoryView(remoteInventory))) }
    }
    return remoteInventory
}

/**
 * 注入事件到 Basic 页面
 */
fun RemoteInventory.inject(basic: Basic) {
    onClick {
        // 处理事件
        try {
            val e = VirtualInventoryInteractEvent(this, VirtualInventoryView(this@inject))
            val event = ClickEvent(e, ClickType.VIRTUAL, basic.getSlot(clickSlot), basic)
            basic.clickCallback.forEach { it(event) }
            basic.selfClickCallback(event)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
    onClose {
        try {
            basic.closeCallback.invoke(InventoryCloseEvent(VirtualInventoryView(this)))
            // 只触发一次
            if (basic.onceCloseCallback) {
                basic.closeCallback = {}
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

/**
 * 获取重新排列后的背包物品（将 0..8 放到最后）
 */
internal fun Player.getStorageItems(): Array<ItemStack?> {
    val storageContents = inventory.storageContents
    return storageContents.sliceArray(9..35) + storageContents.sliceArray(0..8)
}