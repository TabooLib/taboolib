package taboolib.module.ui.receptacle

import org.bukkit.entity.Player

/**
 * @author Arasple
 * @date 2020/12/4 21:20
 */
abstract class NMS {

    abstract fun sendInventoryPacket(player: Player, vararg packets: PacketInventory)
}