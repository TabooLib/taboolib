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
        // 分别收集 x / z 方向的跨格 t 值，便于识别对角"穿角"
        val xBoundaries = sortedSetOf<Double>()
        val zBoundaries = sortedSetOf<Double>()
        addSweepBoundaries(from.x - entity.width / 2.0, dx, xBoundaries)
        addSweepBoundaries(from.x + entity.width / 2.0, dx, xBoundaries)
        addSweepBoundaries(from.z - entity.depth / 2.0, dz, zBoundaries)
        addSweepBoundaries(from.z + entity.depth / 2.0, dz, zBoundaries)
        val boundaries = sortedSetOf(0.0, 1.0)
        boundaries += xBoundaries
        boundaries += zBoundaries
        // 对角移动时 x 边界与 z 边界可能落在同一个 t 上（实体正好从两个方块的公共顶点穿过），
        // 去重后相邻中点采样会落在格子内部，漏掉经典的"穿角"对角穿墙。
        // 故对重合的 t 额外在两侧各取一个采样点，把顶点前后的两个格子都覆盖到。
        val cornerOffset = cornerOffset(dx, dz)
        xBoundaries.forEach { t ->
            if (zBoundaries.any { abs(it - t) < COINCIDENT_TOLERANCE }) {
                val before = t - cornerOffset
                val after = t + cornerOffset
                if (before > 0.0) boundaries += before
                if (after < 1.0) boundaries += after
            }
        }
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

    /**
     * 计算"穿角"额外采样点在参数空间上的偏移量
     * 保证实际偏移的世界距离恒为 [CORNER_SAMPLE_DISTANCE]，不随线段长度放大
     */
    private fun cornerOffset(dx: Double, dz: Double): Double {
        val length = sqrt(dx * dx + dz * dz)
        if (length < 1.0E-6) return 0.0
        return (CORNER_SAMPLE_DISTANCE / length).coerceAtMost(0.25)
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
                if (!isSupportedAtHeight(below.y + NMS.instance.getBlockHeight(below), y)) {
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

    /**
     * 判断支撑方块的顶面高度 [supportY] 是否足以承载脚部位于 [feetY] 的实体
     *
     * 早先的实现用 `abs(supportY - feetY) > 1e-3` 做精确相等判定，
     * 而 [feetY] 来自 `nodeCenter(node)` 恒为整数，[NMS.getBlockHeight] 对
     * 半砖返回 0.5、农田 / 雪层返回 0.9375、空气与非实心方块返回 0.0——
     * 结果只有满方块（高度恰为 1.0）能通过，半砖 / 农田 / 雪层全部被判为不可站立，
     * 平滑因此退化为几乎不删节点的无操作。
     *
     * 现改为区间判定，记 `delta = supportY - feetY`：
     * - `delta > -1.0`：顶面必须高于脚下方块的底面，即脚下方块确有高度。
     *   空气与非实心方块的高度为 0，恰好落在 `delta == -1.0` 上被排除；
     *   半砖（-0.5）、农田 / 雪层（-0.0625）等非满方块则被正确接受。
     * - `delta <= `[SUPPORT_UPPER_TOLERANCE]：顶面不得显著高于脚部，
     *   否则意味着实体被埋在方块里。栅栏一类高于 1 格的支撑仍在容差内。
     */
    @JvmSynthetic
    internal fun isSupportedAtHeight(supportY: Double, feetY: Double): Boolean {
        val delta = supportY - feetY
        return delta > -1.0 + HEIGHT_EPSILON && delta <= SUPPORT_UPPER_TOLERANCE + HEIGHT_EPSILON
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

    /** 允许的向上容差：顶面略高于脚部（如栅栏、贴着台阶边缘）仍可接受 */
    private const val SUPPORT_UPPER_TOLERANCE = 0.5

    /** 高度比较的浮点容差 */
    private const val HEIGHT_EPSILON = 1.0E-3

    /** 判定两个跨格 t 值是否重合的容差 */
    private const val COINCIDENT_TOLERANCE = 1.0E-9

    /** "穿角"额外采样点距离顶点的世界距离 */
    private const val CORNER_SAMPLE_DISTANCE = 1.0E-3
}
