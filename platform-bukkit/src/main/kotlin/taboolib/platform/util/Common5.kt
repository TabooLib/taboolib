package taboolib.platform.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import taboolib.common5.util.Position
import taboolib.common5.util.Quat

fun Position.toLocation(world: World): Location {
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}

fun Location.toPosition(): Position {
    return Position(blockX, blockY, blockZ)
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