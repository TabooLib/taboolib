package taboolib.module.ui.receptacle

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyEvent

/**
 * @author Arasple
 * @date 2020/12/5 21:42
 */
class ReceptacleInteractEvent(val player: Player, val receptacle: Receptacle, val receptacleClickType: ReceptacleClickType, val slot: Int) : ProxyEvent() {

    var itemStack: ItemStack?
        set(value) = receptacle.setItem(value, slot)
        get() = receptacle.getItem(slot)
}