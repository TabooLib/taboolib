package taboolib.expansion.fx

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.expansion.fx.event.EntityHealEntityEvent
import taboolib.module.nms.getI18nName

/** 获取实体的名字 如果没有就返回中文的I18n */
fun LivingEntity.getShowName(player: Player? = null): String {
    if (this.customName != null) {
        return this.customName!!
    }
    return this.getI18nName()
}

/** 治疗实体 */
fun LivingEntity.heal(amount: Double) {
    val max = this.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    if (this.health + amount > max) {
        this.health = max
    } else {
        this.health += amount
    }
}

/** 治疗实体 会触发一个治疗事件 EntityHealEntityEvent */
fun LivingEntity.heal(amount: Double, target: LivingEntity) {
    val callBack = EntityHealEntityEvent(this, target, amount).callBack()
    if (callBack.isCancelled) {
        return
    }
    this.heal(callBack.amount)
}
