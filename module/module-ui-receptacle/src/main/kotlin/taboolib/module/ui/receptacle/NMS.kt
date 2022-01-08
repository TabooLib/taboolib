package taboolib.module.ui.receptacle

import org.bukkit.entity.Player
import taboolib.module.ui.receptacle.operates.OperateInventory

/**
 * @author Arasple
 * @date 2020/12/4 21:20
 */
abstract class NMS {

    abstract fun sendInventoryOperate(player: Player, vararg operates: OperateInventory)
}