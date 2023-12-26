package taboolib.module.ui.virtual

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.type.Basic

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
        Bukkit.getPluginManager().callEvent(InventoryOpenEvent(remoteInventory.createInventoryView()))
    } else {
        submit { Bukkit.getPluginManager().callEvent(InventoryOpenEvent(remoteInventory.createInventoryView())) }
    }
    return remoteInventory
}

/**
 * 注入事件到 Basic 页面
 */
fun RemoteInventory.inject(menu: Basic) {
    onClick {
        // 处理事件
        try {
            val e = VirtualInventoryInteractEvent(this, createInventoryView())
            val event = ClickEvent(e, ClickType.VIRTUAL, menu.getSlot(clickSlot), menu)
            menu.clickCallback.forEach { it(event) }
            menu.selfClickCallback(event)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
    onClose {
        try {
            // 标题更新 && 跳过关闭回调
            if (menu.isUpdateTitle && menu.skipCloseCallbackOnUpdateTitle) {
                return@onClose
            }
            menu.closeCallback.invoke(InventoryCloseEvent(createInventoryView()))
            // 只触发一次
            if (menu.onceCloseCallback) {
                menu.closeCallback = {}
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

/**
 * 生成 InventoryView
 */
fun RemoteInventory.createInventoryView(): InventoryView {
    return try {
        VirtualInventoryView(this)
    } catch (_: LinkageError) {
        VirtualInventoryViewLegacy(object : RemoteInventoryLegacy {

            val bottomInventory = VirtualStorageInventory(inventory)

            override fun inventory(): Inventory {
                return inventory
            }

            override fun bottomInventory(): Inventory {
                return bottomInventory
            }

            override fun viewer(): Player {
                return viewer
            }
        })
    }
}

/**
 * 获取重新排列后的背包物品（将 0..8 放到最后）
 */
internal fun Player.getStorageItems(): Array<ItemStack?> {
    val storageContents = inventory.storageContents
    return storageContents.sliceArray(9..35) + storageContents.sliceArray(0..8)
}