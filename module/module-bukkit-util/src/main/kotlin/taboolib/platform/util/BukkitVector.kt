package taboolib.platform.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import taboolib.common5.Quat

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