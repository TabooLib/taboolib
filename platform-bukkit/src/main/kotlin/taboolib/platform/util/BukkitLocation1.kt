package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.Location

fun taboolib.common.util.Location.toBukkitLocation(): Location {
    return Location(world?.let { Bukkit.getWorld(it) }, x, y, z, yaw, pitch)
}

fun Location.toProxyLocation(): taboolib.common.util.Location {
    return taboolib.common.util.Location(world?.name, x, y, z, yaw, pitch)
}