package taboolib.module.ui.virtual

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.nmsProxy
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.ui.virtual.InventoryHandler
 *
 * @author 坏黑
 * @since 2023/1/15 21:27
 */
abstract class InventoryHandler {

    abstract fun openInventory(player: Player, inventory: VirtualInventory, cursorItem: ItemStack = player.itemOnCursor): RemoteInventory

    companion object {

        val instance by unsafeLazy { nmsProxy<InventoryHandler>() }

        val playerContainerCounterMap = ConcurrentHashMap<String, Int>()

        val playerRemoteInventoryMap = ConcurrentHashMap<String, RemoteInventory>()

        fun getContainerCounter(player: Player): Int {
            val id = playerContainerCounterMap.computeIfAbsent(player.name) { 0 }
            val newId = id % 100 + 1
            playerContainerCounterMap[player.name] = newId
            return newId
        }

        @SubscribeEvent
        private fun onQuit(e: PlayerQuitEvent) {
            playerContainerCounterMap.remove(e.player.name)
            playerRemoteInventoryMap.remove(e.player.name)
        }

        @Ghost
        @SubscribeEvent
        private fun onReceive(e: PacketReceiveEvent) {
            when (e.packet.name) {
                "PacketPlayInCloseWindow" -> {
                    val id = e.packet.read<Int>("a")!!
                    val player = e.player
                    val remoteInventory = playerRemoteInventoryMap[player.name]
                    if (remoteInventory != null && remoteInventory.id == id) {
                        playerRemoteInventoryMap.remove(player.name)?.close(sendPacket = false)
                        player.updateInventory()
                    }
                }
                "PacketPlayInWindowClick" -> {
                    val id = e.packet.read<Int>(if (MinecraftVersion.isUniversal) "containerId" else "a")!!
                    val player = e.player
                    val remoteInventory = playerRemoteInventoryMap[player.name]
                    if (remoteInventory != null && remoteInventory.id == id) {
                        remoteInventory.handleClick(e.packet)
                    }
                }
            }
        }
    }
}

// 1.19

typealias NMSPacketPlayOutWindowItems = net.minecraft.network.protocol.game.PacketPlayOutWindowItems

typealias NMSPacketPlayOutSetSlot = net.minecraft.network.protocol.game.PacketPlayOutSetSlot

typealias NMSItemStack = net.minecraft.world.item.ItemStack

typealias Craft19Container = org.bukkit.craftbukkit.v1_19_R2.inventory.CraftContainer

typealias Craft19ItemStack = org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack

typealias Craft19Player = org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer

// 1.16

typealias NMS16PacketPlayOutOpenWindow = net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindow

typealias NMS16PacketPlayOutWindowItems = net.minecraft.server.v1_16_R3.PacketPlayOutWindowItems

typealias NMS16PacketPlayOutSetSlot = net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot

typealias NMS16PacketPlayOutWindowData = net.minecraft.server.v1_16_R3.PacketPlayOutWindowData

typealias NMS16PacketPlayOutCloseWindow = net.minecraft.server.v1_16_R3.PacketPlayOutCloseWindow

typealias NMS16EntityHuman = net.minecraft.server.v1_16_R3.EntityHuman

typealias NMS16NonNullList<T> = net.minecraft.server.v1_16_R3.NonNullList<T>

typealias NMS16ItemStack = net.minecraft.server.v1_16_R3.ItemStack

typealias Craft16ChatMessage = org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage

typealias Craft16Container = org.bukkit.craftbukkit.v1_16_R3.inventory.CraftContainer

typealias Craft16Player = org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer

typealias Craft16ItemStack = org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack

// 1.9

typealias NMS9PacketPlayOutOpenWindow = net.minecraft.server.v1_9_R2.PacketPlayOutOpenWindow

typealias NMS9PacketPlayOutWindowItems = net.minecraft.server.v1_9_R2.PacketPlayOutWindowItems

typealias NMS9EntityHuman = net.minecraft.server.v1_9_R2.EntityHuman

typealias NMS9ItemStack = net.minecraft.server.v1_9_R2.ItemStack

typealias Craft9ChatMessage = org.bukkit.craftbukkit.v1_9_R2.util.CraftChatMessage

typealias Craft9Container = org.bukkit.craftbukkit.v1_9_R2.inventory.CraftContainer

typealias Craft9ChatComponentText = net.minecraft.server.v1_9_R2.ChatComponentText

typealias Craft9Player = org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer

typealias Craft9ItemStack = org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack