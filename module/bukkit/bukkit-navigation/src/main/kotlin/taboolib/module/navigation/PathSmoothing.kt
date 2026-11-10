package taboolib.module.navigation

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import taboolib.platform.util.callRegion
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 路径平滑后处理（String Pulling / 拉绳法）
 *
 * 将 A* 产生的网格路径转化为平滑路径，去掉多余拐点，
 * 让路径尽可能走直线。
 *
 * @author sky
 * @since 2026/1/30
 */
object PathSmoothing {

    /**
     * 对 A* 路径进行平滑处理
     * 返回平滑后的世界坐标点列表（方块中心）
     */
    fun smooth(path: Path, entity: NodeEntity): List<Vector> {
        if (path.nodes.isEmpty()) {
            return emptyList()
        }
        return entity.location.callRegion {
            smoothAtRegion(path, entity)
        }
    }

    private fun smoothAtRegion(path: Path, entity: NodeEntity): List<Vector> {
        val nodes = path.nodes
        if (nodes.size <= 2) {
            return nodes.map { nodeCenter(it) }
        }
        val world = entity.location.world!!
        val waypoints = mutableListOf(nodeCenter(nodes[0]))
        var current = 0
        val last = nodes.size - 1
        while (current < last) {
            var farthest = current + 1
            for (probe in (current + 2)..last) {
                // y 不同时不跨越高度差
                if (nodes[probe].y != nodes[current].y) break
                if (hasLineOfSightAtRegion(nodeCenter(nodes[current]), nodeCenter(nodes[probe]), entity, world)) {
                    farthest = probe
                }
            }
            waypoints.add(nodeCenter(nodes[farthest]))
            current = farthest
        }
        return waypoints
    }

    /**
     * 检查两点之间是否存在无障碍直线路径
     */
    fun hasLineOfSight(from: Vector, to: Vector, entity: NodeEntity, world: World): Boolean {
        return Location(world, from.x, from.y, from.z).callRegion {
            hasLineOfSightAtRegion(from, to, entity, world)
        }
    }

    private fun hasLineOfSightAtRegion(from: Vector, to: Vector, entity: NodeEntity, world: World): Boolean {
        val dx = to.x - from.x
        val dz = to.z - from.z
        if (abs(dx) < 1.0E-6 && abs(dz) < 1.0E-6) return true
        val boundaries = sortedSetOf(0.0, 1.0)
        addSweepBoundaries(from.x - entity.width / 2.0, dx, boundaries)
        addSweepBoundaries(from.x + entity.width / 2.0, dx, boundaries)
        addSweepBoundaries(from.z - entity.depth / 2.0, dz, boundaries)
        addSweepBoundaries(from.z + entity.depth / 2.0, dz, boundaries)
        val samples = boundaries.toList()
        for (index in samples.indices) {
            val t = samples[index]
            if (!isStandableAtRegion(from.x + dx * t, from.y, from.z + dz * t, entity, world)) return false
            if (index + 1 < samples.size) {
                val midpoint = (t + samples[index + 1]) / 2.0
                if (!isStandableAtRegion(from.x + dx * midpoint, from.y, from.z + dz * midpoint, entity, world)) return false
            }
        }
        return true
    }

    private fun addSweepBoundaries(start: Double, delta: Double, boundaries: MutableSet<Double>) {
        if (abs(delta) < 1.0E-6) return
        val end = start + delta
        val first = floor(min(start, end)).toInt()
        val last = ceil(max(start, end)).toInt()
        for (boundary in first..last) {
            val t = (boundary - start) / delta
            if (t > 0.0 && t < 1.0) {
                boundaries += t
            }
        }
    }

    /**
     * 检查某个世界坐标位置是否可供实体站立
     * - 脚下有支撑（非空气）
     * - 实体碰撞箱范围内无不可通行方块
     */
    fun isStandable(x: Double, y: Double, z: Double, entity: NodeEntity, world: World): Boolean {
        return Location(world, x, y, z).callRegion {
            isStandableAtRegion(x, y, z, entity, world)
        }
    }

    private fun isStandableAtRegion(x: Double, y: Double, z: Double, entity: NodeEntity, world: World): Boolean {
        val halfWidth = entity.width / 2.0
        val halfDepth = entity.depth / 2.0
        val minBx = floor(x - halfWidth).toInt()
        val maxBx = ceil(x + halfWidth).toInt() - 1
        val minBz = floor(z - halfDepth).toInt()
        val maxBz = ceil(z + halfDepth).toInt() - 1
        val by = floor(y).toInt()
        val heightBlocks = ceil(entity.height).toInt()
        if (!isWithinNavigationHeight(by, world.navigationMinHeight(), world.maxHeight)
            || !isWithinNavigationHeight(by + heightBlocks - 1, world.navigationMinHeight(), world.maxHeight)) {
            return false
        }
        val typeFactory = PathTypeFactory(entity)
        for (bx in minBx..maxBx) {
            for (bz in minBz..maxBz) {
                val below = world.getBlockAtIfLoaded(Vector(bx, by - 1, bz)) ?: return false
                val supportY = below.y + NMS.instance.getBlockHeight(below)
                if (abs(supportY - y) > 1.0E-3) {
                    return false
                }
                val feetType = typeFactory.getTypeAsWalkable(world, Vector(bx, by, bz))
                if (!isSafeSmoothingFeetType(feetType, entity.getPathfindingMalus(feetType))) {
                    return false
                }
                for (oy in 0 until heightBlocks) {
                    val bodyType = typeFactory.evaluateType(PathTypeFactory.getRawType(world, Vector(bx, by + oy, bz)))
                    if (!isSafeSmoothingBodyType(entity.getPathfindingMalus(bodyType))) {
                        return false
                    }
                }
            }
        }
        return true
    }

    @JvmSynthetic
    internal fun isSafeSmoothingFeetType(pathType: PathType, malus: Float): Boolean {
        return pathType != PathType.OPEN && malus == 0.0f
    }

    @JvmSynthetic
    internal fun isSafeSmoothingBodyType(malus: Float): Boolean {
        return malus == 0.0f
    }

    private fun nodeCenter(node: Node): Vector {
        return Vector(node.x + 0.5, node.y.toDouble(), node.z + 0.5)
    }
}
