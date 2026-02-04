package taboolib.module.navigation

import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

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

    /** 视线检测采样步长（格） */
    private const val SAMPLE_STEP = 0.5

    /**
     * 对 A* 路径进行平滑处理
     * 返回平滑后的世界坐标点列表（方块中心）
     */
    fun smooth(path: Path, entity: NodeEntity): List<Vector> {
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
                if (hasLineOfSight(nodeCenter(nodes[current]), nodeCenter(nodes[probe]), entity, world)) {
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
        val dx = to.x - from.x
        val dz = to.z - from.z
        val dist = sqrt(dx * dx + dz * dz)
        if (dist < 1e-6) return true
        val steps = ceil(dist / SAMPLE_STEP).toInt()
        for (i in 0..steps) {
            val t = i.toDouble() / steps
            val x = from.x + dx * t
            val z = from.z + dz * t
            if (!isStandable(x, from.y, z, entity, world)) return false
        }
        return true
    }

    /**
     * 检查某个世界坐标位置是否可供实体站立
     * - 脚下有支撑（非空气）
     * - 实体碰撞箱范围内无不可通行方块
     */
    fun isStandable(x: Double, y: Double, z: Double, entity: NodeEntity, world: World): Boolean {
        val halfWidth = entity.width / 2.0
        val halfDepth = entity.depth / 2.0
        val minBx = floor(x - halfWidth).toInt()
        val maxBx = floor(x + halfWidth).toInt()
        val minBz = floor(z - halfDepth).toInt()
        val maxBz = floor(z + halfDepth).toInt()
        val by = floor(y).toInt()
        val heightBlocks = ceil(entity.height).toInt()
        for (bx in minBx..maxBx) {
            for (bz in minBz..maxBz) {
                // 脚下方块必须有支撑
                val below = world.getBlockAtIfLoaded(Vector(bx, by - 1, bz)) ?: return false
                if (below.type.isAirLegacy()) return false
                // 实体身体占据的空间必须可通行
                for (oy in 0 until heightBlocks) {
                    val block = world.getBlockAtIfLoaded(Vector(bx, by + oy, bz)) ?: return false
                    if (block.type.isSolid) return false
                }
            }
        }
        return true
    }

    private fun nodeCenter(node: Node): Vector {
        return Vector(node.x + 0.5, node.y.toDouble(), node.z + 0.5)
    }
}
