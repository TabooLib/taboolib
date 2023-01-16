package taboolib.module.ui.virtual

import net.minecraft.core.NonNullList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.Packet
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir

/**
 * TabooLib
 * taboolib.module.ui.virtual.InventoryHandlerImpl
 *
 * @author 坏黑
 * @since 2023/1/15 21:28
 */
class InventoryHandlerImpl : InventoryHandler() {

    val major = MinecraftVersion.major

    override fun openInventory(player: Player, inventory: VirtualInventory, cursorItem: ItemStack): RemoteInventory {
        val id = getContainerCounter(player)
        when (major) {
            // 1.9, 1.10, 1.11, 1.12
            // public static String getNotchInventoryType(InventoryType type)
            in 1..4 -> {
                val windowType = Craft9Container.getNotchInventoryType(inventory.type)
                val container = Craft9Container(inventory.bukkitInventory, player, id)
                var size = container.bukkitView.topInventory.size
                if (windowType == "minecraft:crafting_table" || windowType == "minecraft:anvil" || windowType == "minecraft:enchanting_table") {
                    size = 0
                }
                val title = container.bukkitView.title
                val packet = NMS9PacketPlayOutOpenWindow(container.windowId, windowType, Craft9ChatComponentText(title), size)
                player.sendPacket(packet)
                return VInventory(inventory, id, player, container, cursorItem, title)
            }
            // 1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19
            // public static Containers getNotchInventoryType(InventoryType type)
            in 5..11 -> {
                val windowType = Craft16Container.getNotchInventoryType(inventory.bukkitInventory)
                val container = Craft16Container(inventory.bukkitInventory, (player as Craft16Player).handle, id)
                val title = container.bukkitView.title
                val packet = NMS16PacketPlayOutOpenWindow(id, windowType, Craft16ChatMessage.fromString(title)[0])
                player.sendPacket(packet)
                return VInventory(inventory, id, player, container, cursorItem, title)
            }
            // 不支持
            else -> error("Unsupported version")
        }
    }

    class VInventory(
        override val inventory: VirtualInventory,
        override val id: Int,
        override val viewer: Player,
        val container: Any,
        val cursorItem: ItemStack,
        override val title: String,
    ) : RemoteInventory {

        val air = ItemStack(Material.AIR)
        val major = MinecraftVersion.major
        var stateId = 0

        var isClosed = false
        var onCloseCallback: (() -> Unit)? = null
        var onClickCallback: (RemoteInventory.ClickEvent.() -> Unit)? = null

        init {
            refresh(inventory.contents.map { it ?: air }, inventory.storageContents, cursorItem)
        }

        @Suppress("CAST_NEVER_SUCCEEDS")
        fun sendInitialData(windowItems: List<ItemStack>, cursorItem: ItemStack) {
            if (isClosed) {
                return
            }
            when (major) {
                // 1.9, 1.10
                // public PacketPlayOutWindowItems(int var1, List<ItemStack> var2)
                in 1..2 -> {
                    viewer.sendPacket(NMS9PacketPlayOutWindowItems(id, windowItems.map { Craft9ItemStack.asNMSCopy(it) }))
                }
                // 1.11, 1.12, 1.13, 1.14, 1.15, 1.16
                // public PacketPlayOutWindowItems(int var1, NonNullList<ItemStack> var2)
                in 3..8 -> {
                    val nmsWindowItems = NMS16NonNullList.a<NMS16ItemStack>()
                    nmsWindowItems.addAll(windowItems.map { Craft16ItemStack.asNMSCopy(it) })
                    viewer.sendPacket(NMS16PacketPlayOutWindowItems(id, nmsWindowItems))
                }
                // 1.17, 1.18, 1.19
                // public PacketPlayOutWindowItems(int var0, int var1, NonNullList<ItemStack> var2, ItemStack var3)
                in 9..11 -> {
                    val nmsWindowItems = NMS16NonNullList.a<NMSItemStack>() as NonNullList<NMSItemStack>
                    nmsWindowItems.addAll(windowItems.map { Craft19ItemStack.asNMSCopy(it) })
                    val nmsCursorItem = Craft19ItemStack.asNMSCopy(cursorItem)
                    viewer.sendPacket(NMSPacketPlayOutWindowItems(id, incrementStateId(), nmsWindowItems, nmsCursorItem))
                }
                // 不支持
                else -> error("Unsupported version")
            }
        }

        override fun refresh(contents: List<ItemStack>, storageContents: List<ItemStack>?, cursorItem: ItemStack) {
            if (isClosed) {
                return
            }
            val items = arrayListOf<ItemStack>()
            // 箱子部分
            items += contents
            // 玩家部分
            if (storageContents == null) {
                items += viewer.getStorageItems().map { it ?: air }
            } else {
                items += storageContents
                items += List(36 - storageContents.size) { air }
            }
            // 初始化页面
            sendInitialData(items, cursorItem)
        }

        override fun sendSlotChange(slot: Int, itemStack: ItemStack) {
            if (isClosed) {
                return
            }
            val id = if (slot == -1) -1 else id
            when (major) {
                // 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16
                // public PacketPlayOutSetSlot(int var1, int var2, ItemStack var3)
                in 1..8 -> {
                    viewer.sendPacket(NMS16PacketPlayOutSetSlot(id, slot, Craft16ItemStack.asNMSCopy(itemStack)))
                }
                // 1.17, 1.18, 1.19
                // public PacketPlayOutSetSlot(int var0, int var1, int var2, ItemStack var3)
                in 9..11 -> {
                    viewer.sendPacket(NMSPacketPlayOutSetSlot(id, incrementStateId(), slot, Craft19ItemStack.asNMSCopy(itemStack)))
                }
                // 不支持
                else -> error("Unsupported version")
            }
        }

        override fun sendCarriedChange(itemStack: ItemStack) {
            sendSlotChange(-1, itemStack)
        }

        fun sendDataChange(slot: Int, state: Int) {
            broadcastDataValue(slot, state)
        }

        fun broadcastDataValue(slot: Int, state: Int) {
            viewer.sendPacket(NMS16PacketPlayOutWindowData(id, slot, state))
        }

        fun incrementStateId(): Int {
            stateId = stateId + 1 and 32767
            return stateId
        }

        override fun onClose(callback: () -> Unit) {
            val beforeCallback = onCloseCallback
            onCloseCallback = {
                beforeCallback?.invoke()
                callback()
            }
        }

        override fun close(sendPacket: Boolean) {
            if (isClosed) {
                return
            }
            isClosed = true
            // 关闭页面
            if (sendPacket) {
                viewer.sendPacket(NMS16PacketPlayOutCloseWindow(id))
            }
            // 处理回调
            if (isPrimaryThread) {
                onCloseCallback?.invoke()
            } else {
                submit { onCloseCallback?.invoke() }
            }
            // 唤起事件
            if (isPrimaryThread) {
                Bukkit.getPluginManager().callEvent(InventoryCloseEvent(VirtualInventoryView(this@VInventory)))
            } else {
                submit { Bukkit.getPluginManager().callEvent(InventoryCloseEvent(VirtualInventoryView(this@VInventory))) }
            }
        }

        override fun onClick(callback: RemoteInventory.ClickEvent.() -> Unit) {
            val beforeCallback = onClickCallback
            onClickCallback = {
                beforeCallback?.invoke(this)
                callback(this)
            }
        }

        override fun handleClick(packet: Packet) {
            when (major) {
                // 1.9, 1.10, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16
                in 1..1 -> {
                    val slot = packet.read<Int>("slot")!!
                    val button = packet.read<Int>("button")!!
                    // val d = packet.read<Short>("d")!!
                    val shift = packet.read<Any>("shift").toString()
                    handle(slot, button, shift)
                }
                // 1.17, 1.18, 1.19
                in 9..11 -> {
                    // val stateId = packet.read<Int>("stateId")!!
                    val slotNum = packet.read<Int>("slotNum")!!
                    val buttonNum = packet.read<Int>("buttonNum")!!
                    val clickType = packet.read<Any>("clickType").toString()
                    handle(slotNum, buttonNum, clickType)
                }
                // 不支持
                else -> error("Unsupported version")
            }
        }

        fun handle(slotNum: Int, buttonNum: Int, clickType: String) {
            val bukkitClickType = when (clickType) {
                // 左右键
                "PICKUP" -> {
                    if (buttonNum == 0) ClickType.LEFT else ClickType.RIGHT
                }
                // SHIFT + 左右键
                "QUICK_MOVE" -> {
                    if (buttonNum == 0) ClickType.SHIFT_LEFT else ClickType.SHIFT_RIGHT
                }
                // 数字键
                "SWAP" -> {
                    if (buttonNum == 40) ClickType.SWAP_OFFHAND else ClickType.NUMBER_KEY
                }
                // 中键
                "CLONE" -> {
                    ClickType.MIDDLE
                }
                // 丢弃
                "THROW" -> {
                    if (slotNum == -999) {
                        if (buttonNum == 0) ClickType.WINDOW_BORDER_LEFT else ClickType.WINDOW_BORDER_RIGHT
                    } else {
                        if (buttonNum == 0) ClickType.DROP else ClickType.CONTROL_DROP
                    }
                }
                // 双击
                "PICKUP_ALL" -> ClickType.DOUBLE_CLICK
                // 拖拽
                else -> ClickType.UNKNOWN
            }
            // 获取点击物品
            val clickItem = when {
                slotNum < 0 || slotNum > inventory.size + 36 -> null
                slotNum < inventory.size -> inventory.getItem(slotNum)
                else -> inventory.getStorageItem(slotNum - inventory.size)
            }
            // 处理回调
            submit { onClickCallback?.invoke(RemoteInventory.ClickEvent(bukkitClickType, slotNum, buttonNum, clickItem ?: air)) }
            // 处理页面
            if (clickItem.isNotAir()) {
                // 一般点击方式
                sendCarriedChange(cursorItem)
                sendSlotChange(slotNum, clickItem!!)
                when (bukkitClickType) {
                    // 数字键（0..8）
                    ClickType.NUMBER_KEY -> {
                        // 刷新背包物品
                        sendSlotChange(inventory.size + 27 + buttonNum, inventory.getStorageItem(27 + buttonNum))
                    }
                    // F 键
                    ClickType.SWAP_OFFHAND -> {
                        // 刷新副手物品
                        viewer.inventory.setItemInOffHand(viewer.inventory.itemInOffHand)
                    }
                    // SHIFT 键
                    ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                        // 获取点击物品数量
                        var amount = clickItem.amount
                        // 从页面移动到背包
                        if (slotNum < inventory.size) {
                            // 从最后一个物品开始检索
                            for (i in 35 downTo 0) {
                                val item = inventory.getStorageItem(i)
                                if (item.isAir) {
                                    // 刷新背包物品
                                    sendSlotChange(inventory.size + i, item)
                                    break
                                }
                                if (item.isSimilar(clickItem)) {
                                    // 扣除物品数量
                                    amount -= item.type.maxStackSize - item.amount
                                    // 刷新背包物品
                                    sendSlotChange(inventory.size + i, item)
                                }
                                if (amount <= 0) {
                                    break
                                }
                            }
                        }
                        // 从物品栏移动到页面
                        else {
                            // 从第一个物品开始检索
                            for (i in 0 until inventory.size) {
                                val item = inventory.getItem(i)
                                if (item.isAir) {
                                    // 刷新页面物品
                                    sendSlotChange(i, item ?: air)
                                    break
                                }
                                if (item!!.isSimilar(clickItem)) {
                                    // 扣除物品数量
                                    amount -= item.type.maxStackSize - item.amount
                                    // 刷新页面物品
                                    sendSlotChange(i, item)
                                }
                            }
                        }
                    }
                    // 其他暂时不管
                    else -> {}
                }
            }
        }
    }
}