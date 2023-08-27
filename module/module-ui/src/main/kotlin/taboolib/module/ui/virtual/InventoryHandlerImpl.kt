package taboolib.module.ui.virtual

import net.minecraft.core.NonNullList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.UnsupportedVersionException
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

    override fun craftChatMessageToPlain(message: Any): String {
        message as NMS16IChatBaseComponent
        return Craft16ChatMessage.fromComponent(message)
    }

    override fun parseToCraftChatMessage(source: String): Any {
        // 不对 1.12 以下版本进行支持
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_12) && source.startsWith('{') && source.endsWith('}')) {
            // 1.16+
            // ChatSerializer.a 的返回值由 IChatBaseComponent 变为 IChatMutableComponent
            try {
                if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_16)) {
                    NMS16ChatSerializer.a(source)!!
                } else {
                    NMS12ChatSerializer.a(source)!!
                }
            } catch (ex: NoSuchMethodError) {
                ex.printStackTrace()
            } catch (ex: Throwable) {
                Craft16ChatMessage.fromString(source)[0]
            }
        } else {
            Craft16ChatMessage.fromString(source)[0]
        }
    }

    override fun openInventory(player: Player, inventory: VirtualInventory, cursorItem: ItemStack): RemoteInventory {
        val id = getContainerCounter(player)
        when (major) {
            // 1.9, 1.10, 1.11, 1.12
            // public static String getNotchInventoryType(InventoryType type)
            in MinecraftVersion.V1_9..MinecraftVersion.V1_12 -> {
                val windowType = Craft9Container.getNotchInventoryType(inventory.type)
                val container = try {
                    Craft9Container(inventory.bukkitInventory, player, id)
                } catch (_: NoSuchMethodError) {
                    // fuck you spigot
                    Craft16Container(inventory.bukkitInventory, (player as Craft16Player).handle, id) as Craft9Container
                }
                var size = container.bukkitView.topInventory.size
                if (windowType == "minecraft:crafting_table" || windowType == "minecraft:anvil" || windowType == "minecraft:enchanting_table") {
                    size = 0
                }
                val title = container.bukkitView.title
                val component = if (title.startsWith('{') && title.endsWith('}')) {
                    runCatching { NMS9IChatBaseComponentChatSerializer.a(title) }.getOrElse { NMS9ChatComponentText(title) }
                } else {
                    NMS9ChatComponentText(title)
                }
                val packet = NMS9PacketPlayOutOpenWindow(container.windowId, windowType, component, size)
                player.sendPacket(packet)
                return VInventory(inventory, id, player, container, cursorItem, title)
            }
            // 1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19, 1.20
            // public static Containers getNotchInventoryType(InventoryType type)
            in MinecraftVersion.V1_13..MinecraftVersion.V1_20 -> {
                val windowType = Craft16Container.getNotchInventoryType(inventory.bukkitInventory)
                val container = Craft16Container(inventory.bukkitInventory, (player as Craft16Player).handle, id)
                val title = container.bukkitView.title
                val component = if (title.startsWith('{') && title.endsWith('}')) {
                    NMS16ChatSerializer.a(title)
                } else {
                    Craft16ChatMessage.fromString(title)[0]
                }
                val packet = NMS16PacketPlayOutOpenWindow(id, windowType, component)
                player.sendPacket(packet)
                return VInventory(inventory, id, player, container, cursorItem, title)
            }
            // 不支持
            else -> throw UnsupportedVersionException()
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
                in MinecraftVersion.V1_9..MinecraftVersion.V1_10 -> {
                    viewer.sendPacket(NMS9PacketPlayOutWindowItems(id, windowItems.map { Craft9ItemStack.asNMSCopy(it) }))
                }
                // 1.11, 1.12, 1.13, 1.14, 1.15, 1.16
                // public PacketPlayOutWindowItems(int var1, NonNullList<ItemStack> var2)
                in MinecraftVersion.V1_13..MinecraftVersion.V1_16 -> {
                    val nmsWindowItems = NMS16NonNullList.a<NMS16ItemStack>()
                    nmsWindowItems.addAll(windowItems.map { Craft16ItemStack.asNMSCopy(it) })
                    viewer.sendPacket(NMS16PacketPlayOutWindowItems(id, nmsWindowItems))
                }
                // 1.17, 1.18, 1.19, 1.20
                // public PacketPlayOutWindowItems(int var0, int var1, NonNullList<ItemStack> var2, ItemStack var3)
                in MinecraftVersion.V1_17..MinecraftVersion.V1_20 -> {
                    val nmsWindowItems = NMS16NonNullList.a<NMSItemStack>() as NonNullList<NMSItemStack>
                    nmsWindowItems.addAll(windowItems.map { Craft19ItemStack.asNMSCopy(it) })
                    val nmsCursorItem = Craft19ItemStack.asNMSCopy(cursorItem)
                    viewer.sendPacket(NMSPacketPlayOutWindowItems(id, incrementStateId(), nmsWindowItems, nmsCursorItem))
                }
                // 不支持
                else -> throw UnsupportedVersionException()
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
                in MinecraftVersion.V1_9..MinecraftVersion.V1_16 -> {
                    viewer.sendPacket(NMS16PacketPlayOutSetSlot(id, slot, Craft16ItemStack.asNMSCopy(itemStack)))
                }
                // 1.17, 1.18, 1.19, 1.20
                // public PacketPlayOutSetSlot(int var0, int var1, int var2, ItemStack var3)
                in MinecraftVersion.V1_17..MinecraftVersion.V1_20 -> {
                    viewer.sendPacket(NMSPacketPlayOutSetSlot(id, incrementStateId(), slot, Craft19ItemStack.asNMSCopy(itemStack)))
                }
                // 不支持
                else -> throw UnsupportedVersionException()
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
                Bukkit.getPluginManager().callEvent(InventoryCloseEvent(createInventoryView()))
            } else {
                submit { Bukkit.getPluginManager().callEvent(InventoryCloseEvent(createInventoryView())) }
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
                in MinecraftVersion.V1_9..MinecraftVersion.V1_16 -> {
                    val slot = packet.read<Int>("slot")!!
                    val button = packet.read<Int>("button")!!
                    // val d = packet.read<Short>("d")!!
                    val shift = packet.read<Any>("shift").toString()
                    handle(slot, button, shift)
                }
                // 1.17, 1.18, 1.19, 1.20
                in MinecraftVersion.V1_17..MinecraftVersion.V1_20 -> {
                    // val stateId = packet.read<Int>("stateId")!!
                    val slotNum = packet.read<Int>("slotNum")!!
                    val buttonNum = packet.read<Int>("buttonNum")!!
                    val clickType = packet.read<Any>("clickType").toString()
                    handle(slotNum, buttonNum, clickType)
                }
                // 不支持
                else -> throw UnsupportedVersionException()
            }
        }

        fun handle(slotNum: Int, buttonNum: Int, clickType: String) {
            val vClickType = when (clickType) {
                // 左右键
                "PICKUP" -> {
                    if (buttonNum == 0) VirtualClickType.LEFT else VirtualClickType.RIGHT
                }
                // SHIFT + 左右键
                "QUICK_MOVE" -> {
                    if (buttonNum == 0) VirtualClickType.SHIFT_LEFT else VirtualClickType.SHIFT_RIGHT
                }
                // 数字键
                "SWAP" -> {
                    if (buttonNum == 40) VirtualClickType.SWAP_OFFHAND else VirtualClickType.NUMBER_KEY
                }
                // 中键
                "CLONE" -> {
                    VirtualClickType.MIDDLE
                }
                // 丢弃
                "THROW" -> {
                    if (slotNum == -999) {
                        if (buttonNum == 0) VirtualClickType.WINDOW_BORDER_LEFT else VirtualClickType.WINDOW_BORDER_RIGHT
                    } else {
                        if (buttonNum == 0) VirtualClickType.DROP else VirtualClickType.CONTROL_DROP
                    }
                }
                // 双击
                "PICKUP_ALL" -> VirtualClickType.DOUBLE_CLICK
                // 拖拽
                else -> VirtualClickType.UNKNOWN
            }
            // 获取点击物品
            val clickItem = when {
                slotNum < 0 || slotNum > inventory.size + 36 -> null
                slotNum < inventory.size -> inventory.getItem(slotNum)
                else -> inventory.getStorageItem(slotNum - inventory.size)
            }
            // 处理回调
            submit { onClickCallback?.invoke(RemoteInventory.ClickEvent(vClickType.toBukkit(), slotNum, buttonNum, clickItem ?: air)) }
            // 处理页面
            if (clickItem.isNotAir()) {
                // 一般点击方式
                sendCarriedChange(cursorItem)
                sendSlotChange(slotNum, clickItem)
                when (vClickType) {
                    // 数字键（0..8）
                    VirtualClickType.NUMBER_KEY -> {
                        // 刷新背包物品
                        sendSlotChange(inventory.size + 27 + buttonNum, inventory.getStorageItem(27 + buttonNum))
                    }
                    // F 键
                    VirtualClickType.SWAP_OFFHAND -> {
                        // 刷新副手物品
                        viewer.inventory.setItemInOffHand(viewer.inventory.itemInOffHand)
                    }
                    // SHIFT 键
                    VirtualClickType.SHIFT_LEFT, VirtualClickType.SHIFT_RIGHT -> {
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

// 1.19

private typealias NMSPacketPlayOutWindowItems = net.minecraft.network.protocol.game.PacketPlayOutWindowItems

private typealias NMSPacketPlayOutSetSlot = net.minecraft.network.protocol.game.PacketPlayOutSetSlot

private typealias NMSItemStack = net.minecraft.world.item.ItemStack

private typealias Craft19Container = org.bukkit.craftbukkit.v1_19_R3.inventory.CraftContainer

private typealias Craft19ItemStack = org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack

private typealias Craft19Player = org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer

// 1.16

private typealias NMS16PacketPlayOutOpenWindow = net.minecraft.server.v1_16_R1.PacketPlayOutOpenWindow

private typealias NMS16PacketPlayOutWindowItems = net.minecraft.server.v1_16_R1.PacketPlayOutWindowItems

private typealias NMS16PacketPlayOutSetSlot = net.minecraft.server.v1_16_R1.PacketPlayOutSetSlot

private typealias NMS16PacketPlayOutWindowData = net.minecraft.server.v1_16_R1.PacketPlayOutWindowData

private typealias NMS16PacketPlayOutCloseWindow = net.minecraft.server.v1_16_R1.PacketPlayOutCloseWindow

private typealias NMS16EntityHuman = net.minecraft.server.v1_16_R1.EntityHuman

private typealias NMS16NonNullList<T> = net.minecraft.server.v1_16_R1.NonNullList<T>

private typealias NMS16ItemStack = net.minecraft.server.v1_16_R1.ItemStack

private typealias NMS16IChatBaseComponent = net.minecraft.server.v1_16_R1.IChatBaseComponent

private typealias NMS16ChatSerializer = net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer

private typealias Craft16ChatMessage = org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage

private typealias Craft16Container = org.bukkit.craftbukkit.v1_16_R1.inventory.CraftContainer

private typealias Craft16Player = org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer

private typealias Craft16ItemStack = org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack

// 1.12

private typealias NMS12ChatSerializer = net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer

// 1.9

private typealias NMS9PacketPlayOutOpenWindow = net.minecraft.server.v1_9_R2.PacketPlayOutOpenWindow

private typealias NMS9PacketPlayOutWindowItems = net.minecraft.server.v1_9_R2.PacketPlayOutWindowItems

private typealias NMS9EntityHuman = net.minecraft.server.v1_9_R2.EntityHuman

private typealias NMS9ItemStack = net.minecraft.server.v1_9_R2.ItemStack

private typealias NMS9ChatComponentText = net.minecraft.server.v1_9_R2.ChatComponentText

private typealias NMS9IChatBaseComponentChatSerializer = net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer

private typealias Craft9ChatMessage = org.bukkit.craftbukkit.v1_9_R2.util.CraftChatMessage

private typealias Craft9Container = org.bukkit.craftbukkit.v1_9_R2.inventory.CraftContainer

private typealias Craft9Player = org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer

private typealias Craft9ItemStack = org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack