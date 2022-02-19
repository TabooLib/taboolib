package taboolib.module.ui.receptacle

import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.sendPacket
import taboolib.module.ui.receptacle.operates.*
import taboolib.platform.util.isAir

/**
 * @author Arasple
 * @date 2020/12/4 21:25
 */
class NMSImpl : NMS() {

    private val emptyItemStack: net.minecraft.server.v1_16_R3.ItemStack? = CraftItemStack.asNMSCopy((ItemStack(Material.AIR)))

    val staticInventories = HashMap<Player, Pair<Int, Inventory>>()

    private val Player.staticContainerId get() = staticInventories[this]?.first
    private val Player.staticInventory get() = staticInventories[this]?.second

    override fun sendInventoryOperate(player: Player, vararg operates: OperateInventory) {
        operates.forEach {
            when (it) {
                // Close Window Packet
                is OperateWindowClose -> {
                    if (it.packet) {
                        player.sendPacket(PacketPlayOutCloseWindow(it.windowId))
                    } else {
                        player.closeInventory()
                        staticInventories.remove(player)
                    }
                }
                // Update Window Slot
                is OperateWindowSetSlot -> {
                    if (it.packet) when {
                        MinecraftVersion.majorLegacy >= 11701 -> {
                            sendPacket(
                                player,
                                PacketPlayOutSetSlot::class.java.unsafeInstance(),
                                "containerId" to it.windowId,
                                "stateId" to -1,
                                "slot" to it.slot,
                                "itemStack" to toNMSCopy(it.itemStack)
                            )
                        }
                        else -> {
                            player.sendPacket(PacketPlayOutSetSlot(it.windowId, it.slot, toNMSCopy(it.itemStack)))
                        }
                    } else {
                        player.staticInventory!!.setItem(it.slot, it.itemStack)
                    }
                }
                // Update Window Items
                is OperateWindowItems -> {
                    val windowId = if (it.packet) it.windowId else player.staticContainerId!!
                    when {
                        MinecraftVersion.majorLegacy >= 11701 -> {
                            sendPacket(
                                player,
                                PacketPlayOutWindowItems::class.java.unsafeInstance(),
                                "containerId" to windowId,
                                "stateId" to 1,
                                "items" to it.items.map { i -> toNMSCopy(i) }.toList(),
                                "carriedItem" to emptyItemStack
                            )
                        }
                        MinecraftVersion.majorLegacy >= 11700 -> {
                            sendPacket(
                                player,
                                PacketPlayOutWindowItems::class.java.unsafeInstance(),
                                "containerId" to windowId,
                                "items" to it.items.map { i -> toNMSCopy(i) }.toList()
                            )
                        }
                        MinecraftVersion.majorLegacy >= 11000 -> {
                            sendPacket(
                                player,
                                PacketPlayOutWindowItems::class.java.unsafeInstance(),
                                "a" to windowId,
                                "b" to it.items.map { i -> toNMSCopy(i) }.toList()
                            )
                        }
                        else -> {
                            sendPacket(
                                player,
                                PacketPlayOutWindowItems::class.java.unsafeInstance(),
                                "a" to windowId,
                                "b" to it.items.map { i -> toNMSCopy(i) }.toTypedArray()
                            )
                        }
                    }

                }
                // Open Window Packet
                is OperateWindowOpen -> {
                    if (it.packet) when {
                        MinecraftVersion.isUniversal -> {
                            sendPacket(
                                player,
                                PacketPlayOutOpenWindow::class.java.unsafeInstance(),
                                "containerId" to it.windowId,
                                "type" to it.type.vanillaId,
                                "title" to CraftChatMessage.fromStringOrNull(it.title)
                            )
                        }
                        MinecraftVersion.majorLegacy >= 11400 -> {
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
                                "b" to it.type.id,
                                "c" to ChatComponentText(it.title),
                                "d" to it.type.containerSize - 1 // Fixed ViaVersion can not view 6x9 menu bug.
                            )
                        }
                    } else {
                        val inventory = Bukkit.createInventory(null, it.type.toBukkitType(), it.title)
                        player.openInventory(inventory)
                        val windowId = player.getProperty<Int>("entity/containerCounter")!!
                        staticInventories[player] = Pair(windowId, inventory)
                    }

                }
            }
        }
    }

    fun toNMSCopy(itemStack: ItemStack?): net.minecraft.server.v1_16_R3.ItemStack? {
        return if (itemStack.isAir()) emptyItemStack else CraftItemStack.asNMSCopy(itemStack)
    }

    fun sendPacket(player: Player, packet: Any, vararg fields: Pair<String, Any?>) {
        fields.forEach { packet.setProperty(it.first, it.second) }
        player.sendPacket(packet)
    }
}