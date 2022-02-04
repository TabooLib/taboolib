package taboolib.module.navigation

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import taboolib.module.nms.MinecraftVersion

fun createPathfinder(nodeEntity: NodeEntity): PathFinder {
    return PathFinder(NodeReader(nodeEntity))
}

fun World.getBlockAt(position: Vector) = getBlockAt(position.blockX, position.blockY, position.blockZ)

fun World.getBlockAtIfLoaded(position: Vector): Block? {
    return if (position.toLocation(this).chunk.isLoaded) {
        getBlockAt(position.blockX, position.blockY, position.blockZ)
    } else {
        null
    }
}

fun Vector.toBlock(world: World) = toLocation(world).block

fun Vector.down() = Vector(x, y - 1, z)

fun Vector.up() = Vector(x, y + 1, z)

fun Vector.hash() = x.toLong() and 67108863L shl 38 or (y.toLong() and 4095L) or (z.toLong() and 67108863L shl 12)

fun Vector.set(x: Int, y: Int, z: Int): Vector {
    setX(x)
    setY(y)
    setZ(z)
    return this
}

fun Vector.distSqr(double1: Double, double2: Double, double3: Double, boolean4: Boolean): Double {
    val double9 = if (boolean4) 0.5 else 0.0
    val double11 = this.x + double9 - double1
    val double13 = this.y + double9 - double2
    val double15 = this.z + double9 - double3
    return double11 * double11 + double13 * double13 + double15 * double15
}

fun Vector.set(x: Double, y: Double, z: Double): Vector {
    setX(NumberConversions.floor(x))
    setY(NumberConversions.floor(y))
    setZ(NumberConversions.floor(z))
    return this
}

fun Vector.distSqr(position: Vector): Double {
    return this.distSqr(position.x, position.y, position.z, true)
}

fun Vector.bottomCenter(): Vector {
    return Vector(this.x + 0.5, this.y, this.z + 0.5)
}

fun Vector.distSqr(position: Vector, boolean2: Boolean): Double {
    return this.distSqr(position.x, position.y, position.z, boolean2)
}

fun Vector.closerThan(position: Vector, double2: Double): Boolean {
    return this.distSqr(position.x, position.y, position.z, true) < double2 * double2
}

fun Location.toCommonVector(): Vector {
    return Vector(this.blockX, this.blockY, this.blockZ)
}

fun Block.isDoor(): Boolean {
    return type.name.run { endsWith("DOOR") || endsWith("DOOR_BLOCK") }
}

fun Block.isIronDoor(): Boolean {
    return type.name.run { endsWith("IRON_DOOR") || endsWith("IRON_DOOR_BLOCK") }
}

fun Block.isClimbable(): Boolean {
    return type.name.run { endsWith("VINE") || endsWith("VINES") || endsWith("LADDER") }
}

fun Block.isOpened(): Boolean {
    return if (MinecraftVersion.major >= 5) {
        (blockData as org.bukkit.block.data.Openable).isOpen
    } else {
        NMS.INSTANCE.isDoorOpened(this)
    }
}

fun Material.isAirLegacy(): Boolean {
    return when {
        MinecraftVersion.major >= 7 -> isAir
        MinecraftVersion.major >= 5 -> {
            when (this) {
                Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.LEGACY_AIR -> true
                else -> false
            }
        }
        else -> this == Material.AIR
    }
}

fun Material.isWater(): Boolean {
    return name.contains("WATER")
}