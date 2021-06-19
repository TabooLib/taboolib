package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.EvokerFangs
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent
import taboolib.common.platform.EventPriority

fun EventPriority.toBukkit() = when (this) {
    EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
    EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
    EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
    EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
    EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
    EventPriority.MONITOR, EventPriority.CUSTOM -> org.bukkit.event.EventPriority.MONITOR
}

fun dispatchCommand(sender: CommandSender?, command: String): Boolean {
    if (sender is Player) {
        val event = PlayerCommandPreprocessEvent((sender as Player?)!!, "/$command")
        Bukkit.getPluginManager().callEvent(event)
        if (!event.isCancelled && event.message.isNotBlank() && event.message.startsWith("/")) {
            return Bukkit.dispatchCommand(event.player, event.message.substring(1))
        }
    } else {
        val e = ServerCommandEvent(sender!!, command)
        Bukkit.getPluginManager().callEvent(e)
        if (!e.isCancelled && e.command.isNotBlank()) {
            return Bukkit.dispatchCommand(e.sender, e.command)
        }
    }
    return false
}

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