package taboolib.module.ui.receptacle

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.ProxyEvent

/**
 * @author Arasple
 * @date 2020/12/5 21:42
 */
class ReceptacleInteractEvent(val player: Player, val receptacle: Receptacle, val receptacleClickType: ReceptacleClickType, val slot: Int) : ProxyEvent() {

    var itemStack: ItemStack?
        set(value) = receptacle.setItem(value, slot)
        get() = receptacle.getItem(slot)

    fun refresh() {
        if (receptacleClickType.isItemMoveable()) {
            receptacle.type.hotBarSlots.forEach { receptacle.refresh(it) }
            receptacle.type.mainInvSlots.forEach { receptacle.refresh(it) }
            receptacle.type.containerSlots.forEach { receptacle.refresh(it) }
        } else {
            receptacle.refresh(slot)
        }
    }
}