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
import taboolib.module.nms.MinecraftVersion
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Chest
import taboolib.module.ui.type.impl.ChestImpl
import taboolib.platform.util.callRegionAsync
import taboolib.platform.util.isOwnedByCurrentRegion
import java.util.concurrent.CompletableFuture

/**
 * 将背包转换为 VirtualInventory 实例
 */
fun Inventory.virtualize(storageContents: List<ItemStack>? = null): VirtualInventory {
    return VirtualInventory(this, storageContents)
}

/**
 * 使玩家打开虚拟页面。
 *
 * **行为变更**：该方法现在要求在持有查看者的线程上调用，否则抛出 [IllegalStateException]。
 * 此前在非主线程调用时会把事件调用 `submit` 出去、函数照常返回 [RemoteInventory]，
 * 但内部需要发包并写入 `playerRemoteInventoryMap`，异步执行本就不安全。
 * 异步场景请改用 `openVirtualInventoryAsync()`、`HumanEntity.openMenu()` 或 `Entity.runTask()`。
 */
fun HumanEntity.openVirtualInventory(inventory: VirtualInventory, updateId: Boolean = true): RemoteInventory {
    check(isOwnedByCurrentRegion()) {
        "Virtual inventory must be opened on the thread that owns the viewer. Use openVirtualInventoryAsync(), HumanEntity.openMenu(), or Entity.runTask() instead."
    }
    val remoteInventory = InventoryHandler.instance.openInventory(this as Player, inventory, ItemStack(Material.AIR), updateId)
    inventory.remoteInventory = remoteInventory
    InventoryHandler.playerRemoteInventoryMap[name] = remoteInventory
    Bukkit.getPluginManager().callEvent(InventoryOpenEvent(remoteInventory.createInventoryView()))
    return remoteInventory
}

/**
 * 在玩家所属线程打开虚拟页面，并通过 Future 非阻塞返回远程页面。
 */
fun HumanEntity.openVirtualInventoryAsync(inventory: VirtualInventory, updateId: Boolean = true): CompletableFuture<RemoteInventory> {
    return callRegionAsync { openVirtualInventory(inventory, updateId) }
}

fun RemoteInventory.inject(menu: Basic) = inject(menu as ChestImpl)

fun RemoteInventory.inject(menu: Chest) = inject(menu as ChestImpl)

/**
 * 注入事件到 Basic 页面
 */
fun <T : ChestImpl> RemoteInventory.inject(menu: T) {
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
            if (menu.isUpdateTitle && menu.isSkipCloseCallbackOnUpdateTitle) {
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
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_21)) {
        VirtualInventoryViewModern.newInstance(object : RemoteInventoryModern {

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

            override fun title(): String {
                return this@createInventoryView.title
            }
        })
    } else try {
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