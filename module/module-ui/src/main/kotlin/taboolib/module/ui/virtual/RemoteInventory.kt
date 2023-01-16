package taboolib.module.ui.virtual

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.Packet

/**
 * TabooLib
 * taboolib.module.ui.virtual.RemoteInventory
 *
 * @author 坏黑
 * @since 2023/1/15 21:14
 */
interface RemoteInventory {

    /** 获取页面 */
    val inventory: VirtualInventory

    /** 所属玩家 */
    val viewer: Player

    /** 页面序号 */
    val id: Int

    /** 标题 */
    val title: String

    /** 刷新页面 */
    fun refresh(contents: List<ItemStack>, storageContents: List<ItemStack>? = null, cursorItem: ItemStack = viewer.itemOnCursor)

    /** 设置物品 */
    fun sendSlotChange(slot: Int, itemStack: ItemStack)

    /** 设置光标物品 */
    fun sendCarriedChange(itemStack: ItemStack)

    /** 点击时回调 */
    fun onClick(callback: ClickEvent.() -> Unit)

    /** 关闭时回调 */
    fun onClose(callback: () -> Unit)

    /** 关闭页面 */
    fun close(sendPacket: Boolean = true)

    /** 处理点击 */
    fun handleClick(packet: Packet)

    /** 点击事件 */
    class ClickEvent(val clickType: ClickType, val clickSlot: Int, val hotbarKey: Int, val clickItem: ItemStack)
}