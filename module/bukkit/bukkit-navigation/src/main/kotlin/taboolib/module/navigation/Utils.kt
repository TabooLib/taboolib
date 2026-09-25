package taboolib.module.navigation

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.callRegion

fun createPathfinder(nodeEntity: NodeEntity): PathFinder {
    return PathFinder(NodeReader(nodeEntity))
}

fun World.getBlockAt(position: Vector): Block {
    return getBlockAt(position.blockX, position.blockY, position.blockZ)
}

fun World.getBlockAtIfLoaded(position: Vector): Block? {
    val x = position.blockX
    val y = position.blockY
    val z = position.blockZ
    if (!isWithinNavigationHeight(y, navigationMinHeight(), maxHeight)) {
        return null
    }
    return callRegion(x, y, z) {
        if (ChunkAccess.instance.isChunkLoaded(this, x shr 4, z shr 4)) {
            getBlockAt(x, y, z)
        } else {
            null
        }
    }
}

@JvmSynthetic
internal fun World.navigationMinHeight(): Int {
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_17)) minHeight else 0
}

@JvmSynthetic
internal fun isWithinNavigationHeight(y: Int, minHeight: Int, maxHeight: Int): Boolean {
    return y >= minHeight && y < maxHeight
}

fun Vector.toBlock(world: World) = toLocation(world).block

fun Vector.down() = Vector(x, y - 1, z)

fun Vector.up() = Vector(x, y + 1, z)

fun Vector.hash() = ((x.toLong() and 0x3FFFFFFL) shl 38) or (y.toLong() and 0xFFFL) or (((z.toLong() and 0x3FFFFFFL)) shl 12)

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
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
        (blockData as org.bukkit.block.data.Openable).isOpen
    } else {
        NMS.instance.isDoorOpened(this)
    }
}

@Suppress("DEPRECATION")
fun Material.isAirLegacy(): Boolean {
    return when {
        MinecraftVersion.major >= 7 -> isAir
        MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13) -> {
            when (this) {
                Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.LEGACY_AIR -> true
                else -> false
            }
        }
        else -> this == Material.AIR
    }
}

fun Material.isWater(): Boolean {
    return name == "WATER" || name == "STATIONARY_WATER" || name == "FLOWING_WATER"
}

fun Block.isTrapdoorOpen(): Boolean {
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
        (blockData as org.bukkit.block.data.type.TrapDoor).isOpen
    } else {
        NMS.instance.isTrapdoorOpen(this)
    }
}

fun Block.isCampfireLit(): Boolean {
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_14)) {
        (blockData as org.bukkit.block.data.type.Campfire).isLit
    } else {
        true
    }
}

fun Block.isBottomSlab(): Boolean {
    return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
        val slab = blockData as org.bukkit.block.data.type.Slab
        slab.type == org.bukkit.block.data.type.Slab.Type.BOTTOM
    } else {
        NMS.instance.isBottomSlab(this)
    }
}
