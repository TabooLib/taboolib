package taboolib.expansion.fx.event

import org.bukkit.entity.LivingEntity
import taboolib.platform.type.BukkitProxyEvent

data class EntityHealEntityEvent(
    val entity: LivingEntity,
    val target: LivingEntity?,
    val amount: Double
) : BukkitProxyEvent() {
    fun callBack(): EntityHealEntityEvent {
        this.call()
        return this
    }
}
