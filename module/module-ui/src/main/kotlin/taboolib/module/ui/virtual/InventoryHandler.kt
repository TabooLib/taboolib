package taboolib.module.ui.virtual

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Ghost
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
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

    abstract fun craftChatMessageToPlain(message: Any): String

    abstract fun parseToCraftChatMessage(source: String): Any
    
    abstract fun openInventory(player: Player, inventory: VirtualInventory, cursorItem: ItemStack = player.itemOnCursor): RemoteInventory

    @PlatformSide([Platform.BUKKIT])
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

        @Awake(LifeCycle.DISABLE)
        private fun onDisable() {
            Bukkit.getOnlinePlayers().forEach {
                if (playerRemoteInventoryMap.containsKey(it.name)) {
                    playerRemoteInventoryMap[it.name]?.close()
                }
            }
        }

        @Suppress("UnstableApiUsage")
        @Ghost
        @SubscribeEvent
        private fun onReceive(e: PacketReceiveEvent) {
            // 如果没有正在开启的页面则不处理
            if (playerRemoteInventoryMap.isEmpty()) {
                return
            }
            when (e.packet.name) {
                "PacketPlayInCloseWindow" -> {
                    val id = e.packet.read<Int>(if (MinecraftVersion.isUniversal) "containerId" else "id")!!
                    val player = e.player
                    val remoteInventory = playerRemoteInventoryMap[player.name]
                    if (remoteInventory != null && remoteInventory.id == id) {
                        playerRemoteInventoryMap.remove(player.name)?.close(sendPacket = false)
                        try {
                            player.updateInventory()
                        } catch (ex: NoSuchMethodError) {
                            ex.printStackTrace()
                        }
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