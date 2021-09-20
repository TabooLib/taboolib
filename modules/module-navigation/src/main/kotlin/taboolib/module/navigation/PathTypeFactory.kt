package taboolib.module.navigation

import org.bukkit.World
import org.bukkit.util.Vector
import taboolib.module.navigation.Fluid.Companion.getFluid
import java.util.*
import kotlin.math.ceil

/**
 * Navigation
 * ink.ptms.navigation.v2.PathTypeFactory
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
@Suppress("LiftReturnOrAssignment")
open class PathTypeFactory(val entity: NodeEntity) {

    val world = entity.location.world!!

    /**
     * 评估类型
     * 根据实体自身条件判断是否可以穿过该方块
     */
    open fun evaluateType(pathType: PathType): PathType {
        return when {
            pathType == PathType.DOOR_WOOD_CLOSED && entity.canOpenDoors && entity.canPassDoors -> {
                PathType.WALKABLE_DOOR
            }
            pathType == PathType.DOOR_OPEN && !entity.canPassDoors -> {
                PathType.BLOCKED
            }
            pathType == PathType.LEAVES -> {
                PathType.BLOCKED
            }
            else -> pathType
        }
    }

    /**
     * 获取方块类型
     * 主要目的是判断实体的碰撞箱是否允许通过该空间
     */
    open fun getTypeAsBoundingBox(x: Int, y: Int, z: Int): PathType {
        val cover = EnumSet.noneOf(PathType::class.java)
        val coverType = getTypeAsBoundingBox(x, y, z, cover)
        if (cover.contains(PathType.FENCE)) {
            return PathType.FENCE
        }
        var passable = PathType.BLOCKED
        cover.forEach {
            // 假设实体无法通过该方块
            // 则返回该方块类型
            if (entity.getPathfindingMalus(it) < 0.0f) {
                return it
            }
            // 当实体能够通过该方块
            // 则记录该方块
            if (entity.getPathfindingMalus(it) >= 0) {
                passable = it
            }
        }
        // 假设中心方块可通过，且附近无危险方块，怪物宽度小于等于 1 格
        // 则允许通过
        if (coverType === PathType.OPEN && entity.getPathfindingMalus(passable) == 0.0f && entity.width <= 1) {
            return PathType.OPEN
        } else {
            // 否则返回危险方块
            return passable
        }
    }

    /**
     * 获取方块类型
     * 主要目的是判断实体的碰撞箱是否允许通过该空间
     *
     * @param x x
     * @param y y
     * @param z z
     * @param cover 实体自身碰撞箱覆盖的所有方块类型
     */
    open fun getTypeAsBoundingBox(x: Int, y: Int, z: Int, cover: EnumSet<PathType>): PathType {
        var pathType: PathType? = null
        (0 until ceil(entity.width).toInt()).forEach { ox ->
            (0 until ceil(entity.height).toInt()).forEach { oy ->
                (0 until ceil(entity.depth).toInt()).forEach { oz ->
                    // 获取方块类型并评估
                    val type = evaluateType(getTypeAsWalkable(world, Vector(ox + x, oy + y, oz + z))).also {
                        cover.add(it)
                    }
                    // 如果是原点则作为方法的返回值
                    if (ox == 0 && oy == 0 && oz == 0) {
                        pathType = type
                    }
                }
            }
        }
        return pathType!!
    }

    /**
     * 获取方块类型
     * 主要目的为判断方块是否可行走及其行走代价
     *
     * 假设该方块的临近方块存在危险类型
     * 那么该方块也会被视为危险类型
     */
    open fun getTypeAsWalkable(world: World, position: Vector): PathType {
        // 获取原始类型
        var rawType = getRawType(world, position)
        // 当方块可以通过且高度 > 1
        if (rawType == PathType.OPEN && position.y >= 1) {
            // 获取下方方块
            val down = getRawType(world, position.down())
            // 对下方方块进行一个初步的判断
            if (down != PathType.WALKABLE && down != PathType.OPEN && down != PathType.WATER && down != PathType.LAVA) {
                // WALKABLE 类型的唯一来源，代表方块绝对可站立，但危险等级不知。
                rawType = PathType.WALKABLE
            } else {
                rawType = PathType.OPEN
            }
            rawType = when (down) {
                PathType.DAMAGE_FIRE -> PathType.DAMAGE_FIRE
                PathType.DAMAGE_CACTUS -> PathType.DAMAGE_CACTUS
                PathType.DAMAGE_OTHER -> PathType.DAMAGE_OTHER
                PathType.STICKY_HONEY -> PathType.STICKY_HONEY
                else -> rawType
            }
        }
        if (rawType == PathType.WALKABLE) {
            // 临近的危险方块将会代替自身返回
            rawType = getTypeAsNeighbor(world, position, rawType)
        }
        return rawType
    }

    companion object {

        /**
         * 获取方块类型
         * 主要目的是获取其临近的危险方块
         */
        fun getTypeAsNeighbor(world: World, position: Vector, pathType: PathType): PathType {
            (-1..1).forEach { ox ->
                (-1..1).forEach { oy ->
                    (-1..1).forEach { oz ->
                        if (ox != 0 || oz != 0) {
                            val block = world.getBlockAtIfLoaded(Vector(position.x + ox, position.y + oy, position.z + oz))
                            if (block != null) {
                                val name = block.type.name
                                when {
                                    name == "CACTUS" -> {
                                        return PathType.DANGER_CACTUS
                                    }
                                    name == "SWEET_BERRY_BUSH" -> {
                                        return PathType.DANGER_OTHER
                                    }
                                    name.getFluid().isLava() || name in arrayOf("FIRE", "MAGMA_BLOCK", "CAMPFIRE", "SOUL_CAMPFIRE") -> {
                                        return PathType.DANGER_FIRE
                                    }
                                    name.getFluid().isWater() -> {
                                        return PathType.WATER_BORDER
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return pathType
        }

        /**
         * 获取单个方块当原始类型
         * 不对其临近方块进行判断
         */
        fun getRawType(world: World, position: Vector): PathType {
            val block = world.getBlockAtIfLoaded(position) ?: return PathType.BLOCKED
            val blockType = block.type
            val blockTypeName = blockType.name
            return when {
                // 空气
                blockType.isAirLegacy() -> {
                    PathType.OPEN
                }
                // 活板门、睡莲、地毯
                blockTypeName.endsWith("TRAPDOOR") || blockTypeName.endsWith("TRAP_DOOR") || blockTypeName == "LILY_PAD" || blockTypeName == "CARPET" -> {
                    // 能够行走，能够穿过。
                    PathType.TRAPDOOR
                }
                // 栅栏，石墙，关闭的栅栏门
                blockTypeName.endsWith("FENCE") || blockTypeName.endsWith("WALL") || (blockTypeName.endsWith("FENCE_GATE") && !block.isOpened()) -> {
                    // 与 Blocked 不同，Fence 拥有 1.5 格高度无法越过。
                    PathType.FENCE
                }
                // 树叶
                blockTypeName.endsWith("LEAVES") || blockTypeName.endsWith("LEAVES_2") -> {
                    // 与 Blocked 相同，保留类型
                    PathType.LEAVES
                }
                // 仙人掌
                blockTypeName.endsWith("CACTUS") -> {
                    PathType.DAMAGE_CACTUS
                }
                // 浆果从
                blockTypeName == "SWEET_BERRY_BUSH" -> {
                    PathType.DAMAGE_OTHER
                }
                // 蜂蜜块
                blockTypeName == "HONEY_BLOCK" -> {
                    PathType.STICKY_HONEY
                }
                // 可可豆
                blockTypeName.endsWith("COCOA") -> {
                    PathType.COCOA
                }
                // 燃烧物
                blockTypeName in arrayOf("FIRE", "MAGMA_BLOCK", "CAMPFIRE", "SOUL_CAMPFIRE") -> {
                    // 可以穿过，但会受伤
                    PathType.DAMAGE_FIRE
                }
                // 铁门
                block.isIronDoor() -> {
                    if (block.isOpened()) PathType.DOOR_OPEN else PathType.DOOR_IRON_CLOSED
                }
                // 木门
                block.isDoor() -> {
                    if (block.isOpened()) PathType.DOOR_OPEN else PathType.DOOR_WOOD_CLOSED
                }
                // 水
                blockTypeName.getFluid().isWater() -> {
                    // 可以越过，但会判断条件
                    PathType.WATER
                }
                // 岩浆
                blockTypeName.getFluid().isLava() -> {
                    // 不可越过，但会判断条件
                    PathType.LAVA
                }
                // 其他实体方块
                block.type.isSolid -> {
                    // 不可通过，允许越过
                    PathType.BLOCKED
                }
                else -> {
                    PathType.OPEN
                }
            }
        }
    }
}