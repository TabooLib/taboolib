package taboolib.module.navigation

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.Vector
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
        if (coverType == PathType.OPEN && entity.getPathfindingMalus(passable) == 0.0f && entity.width <= 1) {
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
        if (rawType == PathType.OPEN) {
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

        /** 材质 → PathType 静态缓存 */
        val materialTypes: EnumMap<Material, PathType> = EnumMap(Material::class.java)

        /** 邻居危险检测用缓存 */
        val dangerMaterials: EnumMap<Material, PathType> = EnumMap(Material::class.java)

        /** 需要运行时检查方块状态的材质（门、栅栏门） */
        val needsStateCheck: HashSet<Material> = hashSetOf()

        init {
            for (mat in Material.values()) {
                val name = mat.name
                // 门和栅栏门需要运行时状态检查
                if (name.endsWith("DOOR") || name.endsWith("DOOR_BLOCK") || (name.endsWith("FENCE_GATE"))) {
                    needsStateCheck.add(mat)
                    continue
                }
                val type = classifyMaterial(mat, name)
                if (type != null) {
                    materialTypes[mat] = type
                }

                // 邻居危险检测
                val danger = classifyDanger(name)
                if (danger != null) {
                    dangerMaterials[mat] = danger
                }
            }
        }

        private fun classifyMaterial(mat: Material, name: String): PathType? {
            return when {
                mat.isAirLegacy() -> PathType.OPEN
                // 水
                name == "WATER" || name == "FLOWING_WATER" || name == "STATIONARY_WATER" -> PathType.WATER
                // 岩浆
                name == "LAVA" || name == "FLOWING_LAVA" || name == "STATIONARY_LAVA" -> PathType.LAVA
                // 燃烧物
                name == "FIRE" || name == "MAGMA_BLOCK" || name == "CAMPFIRE" || name == "SOUL_CAMPFIRE" -> PathType.DAMAGE_FIRE
                // 仙人掌
                name == "CACTUS" -> PathType.DAMAGE_CACTUS
                // 浆果丛
                name == "SWEET_BERRY_BUSH" -> PathType.DAMAGE_OTHER
                // 蜂蜜块
                name == "HONEY_BLOCK" -> PathType.STICKY_HONEY
                // 可可豆
                name.endsWith("COCOA") -> PathType.COCOA
                // 树叶
                name.endsWith("LEAVES") || name.endsWith("LEAVES_2") -> PathType.LEAVES
                // 栅栏、石墙（不含栅栏门，已在 needsStateCheck）
                name.endsWith("FENCE") || name.endsWith("WALL") -> PathType.FENCE
                // 活板门、睡莲、地毯等可穿过方块
                name.endsWith("TRAPDOOR") || name.endsWith("TRAP_DOOR")
                        || name == "LILY_PAD"
                        || name == "CARPET"
                        || name.endsWith("SAPLING")
                        || name == "REDSTONE_WIRE"
                        || (name.endsWith("GRASS") && !mat.isSolid)
                        || name == "NETHER_WARTS"
                        || name == "NETHER_STALK"
                        || name == "DOUBLE_PLANT"
                        || name.startsWith("FLOWER_POT")
                        || name == "RED_ROSE"
                        || name == "YELLOW_FLOWER"
                        || name == "BEETROOT_BLOCK"
                        || name.startsWith("DIODE_BLOCK")
                        || name == "SUGAR_CANE_BLOCK" -> PathType.OPEN
                // 实体方块
                mat.isSolid -> PathType.BLOCKED
                // 其余
                else -> PathType.OPEN
            }
        }

        private fun classifyDanger(name: String): PathType? {
            return when {
                name == "CACTUS" -> PathType.DANGER_CACTUS
                name == "SWEET_BERRY_BUSH" -> PathType.DANGER_OTHER
                name == "FIRE" || name == "MAGMA_BLOCK" || name == "CAMPFIRE" || name == "SOUL_CAMPFIRE"
                        || name == "LAVA" || name == "FLOWING_LAVA" || name == "STATIONARY_LAVA" -> PathType.DANGER_FIRE
                name == "WATER" || name == "FLOWING_WATER" || name == "STATIONARY_WATER" -> PathType.WATER_BORDER
                else -> null
            }
        }

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
                                val danger = dangerMaterials[block.type]
                                if (danger != null) {
                                    return danger
                                }
                            }
                        }
                    }
                }
            }
            return pathType
        }

        /**
         * 获取单个方块的原始类型
         * 不对其临近方块进行判断
         */
        fun getRawType(world: World, position: Vector): PathType {
            val block = world.getBlockAtIfLoaded(position) ?: return PathType.BLOCKED
            val material = block.type
            // 快速路径：查缓存
            materialTypes[material]?.also { return it }
            // 慢速路径：需要方块状态的材质（门、栅栏门）
            if (material in needsStateCheck) {
                return getStateBasedType(block)
            }
            return PathType.OPEN
        }

        private fun getStateBasedType(block: Block): PathType {
            val name = block.type.name
            // 打开的栅栏门视为可通过
            if (name.endsWith("FENCE_GATE")) {
                return if (block.isOpened()) PathType.OPEN else PathType.FENCE
            }
            // 铁门
            if (block.isIronDoor()) {
                return if (block.isOpened()) PathType.DOOR_OPEN else PathType.DOOR_IRON_CLOSED
            }
            // 木门
            if (block.isDoor()) {
                return if (block.isOpened()) PathType.DOOR_OPEN else PathType.DOOR_WOOD_CLOSED
            }
            return PathType.OPEN
        }
    }
}
