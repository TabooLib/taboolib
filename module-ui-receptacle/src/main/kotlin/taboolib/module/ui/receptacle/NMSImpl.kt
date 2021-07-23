package taboolib.module.ui.receptacle

import net.minecraft.server.v1_16_R3.*
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket
import taboolib.platform.util.isAir

/**
 * @author Arasple
 * @date 2020/12/4 21:25
 */
class NMSImpl : NMS() {

    private val emptyItemStack = CraftItemStack.asNMSCopy((ItemStack(Material.AIR)))

    override fun sendInventoryPacket(player: Player, vararg packets: PacketInventory) {
        packets.forEach {
            when (it) {
                // Close Window Packet
                is PacketWindowClose -> {
                    player.sendPacket(PacketPlayOutCloseWindow(it.windowId))
                }
                // Update Window Slot
                is PacketWindowSetSlot -> {
                    player.sendPacket(PacketPlayOutSetSlot(it.windowId, it.slot, toNMSCopy(it.itemStack)))
                }
                // Update Window Items
                is PacketWindowItems -> {
                    sendPacket(
                        player,
                        PacketPlayOutWindowItems::class.java.unsafeInstance(),
                        "a" to it.windowId,
                        "b" to it.items.map { i -> toNMSCopy(i) }.toList()
                    )
                }
                // Open Window Packet
                is PacketWindowOpen -> {
                    when {
                        MinecraftVersion.isUniversal -> {
                            sendPacket(
                                player,
                                PacketPlayOutOpenWindow::class.java.unsafeInstance(),
                                "containerId" to it.windowId,
                                "type" to it.type.vanillaId,
                                "title" to CraftChatMessage.fromStringOrNull(it.title)
                            )
                        }
                        MinecraftVersion.majorLegacy >= 11300 -> {
                            sendPacket(
                                player,
                                PacketPlayOutOpenWindow(),
                                "a" to it.windowId,
                                "b" to it.type.vanillaId,
                                "c" to CraftChatMessage.fromStringOrNull(it.title)
                            )
                        }
                        else -> {
                            sendPacket(
                                player,
                                PacketPlayOutOpenWindow(),
                                "a" to it.windowId,
                                "b" to it.type.vanillaId,
                                "c" to ChatComponentText(it.title),
                                "d" to it.type.containerSize
                            )
                        }
                    }
                }
            }
        }
    }

    fun toNMSCopy(itemStack: ItemStack?): net.minecraft.server.v1_16_R3.ItemStack? {
        return if (itemStack.isAir()) emptyItemStack else CraftItemStack.asNMSCopy(itemStack)
    }

    fun sendPacket(player: Player, packet: Any, vararg fields: Pair<String, Any>) {
        fields.forEach { packet.setProperty(it.first, it.second) }
        player.sendPacket(packet)
    }
}