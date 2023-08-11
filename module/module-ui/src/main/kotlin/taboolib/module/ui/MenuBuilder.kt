package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerBukkitListener
import taboolib.module.nms.PacketSendEvent
import taboolib.module.ui.virtual.InventoryHandler
import taboolib.module.ui.virtual.VirtualInventory
import taboolib.module.ui.virtual.inject
import taboolib.module.ui.virtual.openVirtualInventory
import taboolib.platform.util.isNotAir

var isRawTitleInVanillaInventoryEnabled = false
    private set

/**
 * 允许在 Vanilla Inventory 中使用 Raw Title
 *
 * 为什么需要主动启用?
 * 1. 一旦注册 [PacketSendEvent] 事件，就需要注入玩家的 Channel 来启用数据包系统。
 * 2. 对于没有这类需求的用户来说，安装 module-ui 就意味着被迫启用数据包系统，造成不必要的性能损耗。
 *
 * 虚拟菜单不需要开启该选项
 */
fun enableRawTitleInVanillaInventory() {
    isRawTitleInVanillaInventoryEnabled = true
    registerBukkitListener(PacketSendEvent::class.java) { e ->
        if (e.packet.name == "PacketPlayOutOpenWindow") {
            // 全版本都是 c，不错
            val plain = InventoryHandler.instance.craftChatMessageToPlain(e.packet.read("c", remap = false)!!)
            if (plain.startsWith('{') && plain.endsWith('}')) {
                e.packet.write("c", InventoryHandler.instance.parseToCraftChatMessage(plain))
            }
        }
    }
}

/**
 * 构建一个菜单
 */
inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    return T::class.java.getDeclaredConstructor(String::class.java).newInstance(title).also(builder).build()
}

/**
 * 构建一个菜单并为玩家打开
 */
inline fun <reified T : Menu> Player.openMenu(title: String = "chest", builder: T.() -> Unit) {
    try {
        val buildMenu = buildMenu(title, builder)
        if (buildMenu is VirtualInventory) {
            val remoteInventory = openVirtualInventory(buildMenu)
            val basic = MenuHolder.fromInventory(buildMenu)
            if (basic != null) {
                remoteInventory.inject(basic)
            }
        } else {
            openInventory(buildMenu)
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 获取当前点击事件下所有受影响的物品
 */
fun InventoryClickEvent.getAffectItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>()
    if (click == ClickType.NUMBER_KEY) {
        val hotbarButton = whoClicked.inventory.getItem(hotbarButton)
        if (hotbarButton.isNotAir()) {
            items += hotbarButton
        }
    }
    if (currentItem.isNotAir()) {
        items += currentItem!!
    }
    return items
}