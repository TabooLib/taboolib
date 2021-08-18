package taboolib.module.ui.receptacle

import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy

/**
 * @author Arasple
 * @date 2020/12/4 21:22
 */
interface PacketInventory {

    fun send(player: Player) = instance.sendInventoryPacket(player, this)

}

val instance = nmsProxy<NMS>()