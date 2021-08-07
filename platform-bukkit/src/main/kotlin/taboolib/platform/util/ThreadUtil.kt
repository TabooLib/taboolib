package taboolib.platform.util

import org.bukkit.Bukkit
import taboolib.platform.BukkitPlugin

fun async(func: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(BukkitPlugin.getInstance(), func)
}

fun sync(func: () -> Unit) {
    Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), func)
}

fun async(time: Long, func: () -> Unit) {
    Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitPlugin.getInstance(), func, time)
}

fun sync(time: Long, func: () -> Unit) {
    Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), func, time)
}
