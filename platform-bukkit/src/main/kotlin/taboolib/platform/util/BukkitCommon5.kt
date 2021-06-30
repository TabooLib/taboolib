package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.World
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common5.Quat

fun taboolib.common.util.Location.toBukkitLocation(): Location {
    return Location(world?.let { Bukkit.getWorld(it) }, x, y, z)
}

fun Location.toProxyLocation(): taboolib.common.util.Location {
    return taboolib.common.util.Location(world?.name, x, y, z, yaw, pitch)
}

fun Quat.toVector(): Vector {
    return Vector(x(), y(), z())
}

fun Quat.toLocation(world: World): Location {
    return Location(world, x(), y(), z())
}

fun Vector.toQuat(): Quat {
    return Quat.at(x, y, z)
}

fun Location.toQuat(): Quat {
    return Quat.at(x, y, z)
}