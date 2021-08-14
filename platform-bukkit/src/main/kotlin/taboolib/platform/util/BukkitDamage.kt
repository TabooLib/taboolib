@file:Isolated
package taboolib.platform.util

import org.bukkit.entity.EvokerFangs
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.Isolated

val EntityDamageByEntityEvent.attacker: LivingEntity?
    get() = if (damager is LivingEntity) {
        damager as LivingEntity
    } else if (damager is Projectile && (damager as Projectile).shooter is LivingEntity) {
        (damager as Projectile).shooter as LivingEntity?
    } else if (damager.javaClass.simpleName == "EvokerFangs" && damager is EvokerFangs) {
        (damager as EvokerFangs).owner
    } else {
        null
    }