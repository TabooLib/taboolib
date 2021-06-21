package taboolib.platform.util

import cn.nukkit.command.CommandSender
import cn.nukkit.event.player.PlayerCommandPreprocessEvent
import cn.nukkit.event.server.ServerCommandEvent
import cn.nukkit.player.Player
import taboolib.common.platform.EventPriority
import taboolib.platform.NukkitPlugin

fun EventPriority.toNukkit() = when (this) {
    EventPriority.LOWEST -> cn.nukkit.event.EventPriority.LOWEST
    EventPriority.LOW -> cn.nukkit.event.EventPriority.LOW
    EventPriority.NORMAL -> cn.nukkit.event.EventPriority.NORMAL
    EventPriority.HIGH -> cn.nukkit.event.EventPriority.HIGH
    EventPriority.HIGHEST -> cn.nukkit.event.EventPriority.HIGHEST
    EventPriority.MONITOR -> cn.nukkit.event.EventPriority.MONITOR
}

fun dispatchCommand(sender: CommandSender, command: String): Boolean {
    if (sender is Player) {
        val event = PlayerCommandPreprocessEvent((sender as Player?)!!, "/$command")
        NukkitPlugin.instance.server.pluginManager.callEvent(event)
        if (!event.isCancelled && event.message.isNotBlank() && event.message.startsWith("/")) {
            return NukkitPlugin.instance.server.dispatchCommand(event.player, event.message.substring(1))
        }
    } else {
        val e = ServerCommandEvent(sender, command)
        NukkitPlugin.instance.server.pluginManager.callEvent(e)
        if (!e.isCancelled && e.command.isNotBlank()) {
            return NukkitPlugin.instance.server.dispatchCommand(e.sender, e.command)
        }
    }
    return false
}