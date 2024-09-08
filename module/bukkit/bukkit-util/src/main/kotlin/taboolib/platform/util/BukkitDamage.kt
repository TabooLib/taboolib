package taboolib.platform.util

import org.bukkit.entity.Damageable
import org.bukkit.entity.EvokerFangs
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent

fun Damageable.kill() {
    health = 0.0
}

val EntityDamageByEntityEvent.attacker: LivingEntity?
    get() {
        val attacker = damager
        return when {
            attacker is LivingEntity -> attacker
            // 弹射物
            attacker is Projectile && attacker.shooter is LivingEntity -> attacker.shooter as LivingEntity?
            // 版本兼容策略
            attacker.javaClass.simpleName == "EvokerFangs" && attacker is EvokerFangs -> attacker.owner
            // 其他
            else -> null
        }
    }

val EntityDeathEvent.killer: LivingEntity?
    get() = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker

val PlayerDeathEvent.killer: LivingEntity?
    get() = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker