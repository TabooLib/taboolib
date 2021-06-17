package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent

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