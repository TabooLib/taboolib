package taboolib.module.ui.receptacle.operates

import org.bukkit.entity.Player
import taboolib.module.nms.nmsProxy
import taboolib.module.ui.receptacle.NMS

/**
 * @author Arasple
 * @date 2020/12/4 21:22
 */
abstract class OperateInventory {

    abstract val packet: Boolean

    open fun send(player: Player) {
        nmsProxy<NMS>().sendInventoryOperate(player, this)
    }
}