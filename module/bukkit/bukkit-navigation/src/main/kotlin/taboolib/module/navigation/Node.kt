package taboolib.module.navigation

import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Navigation
 * taboolib.module.navigation.Node
 * net.minecraft.world.level.pathfinder.Node
 *
 * @author sky
 * @since 2021/2/21 6:29 下午
 */
open class Node(val x: Int, val y: Int, val z: Int) {

    // 哈希值
    val hash = createHash(x, y, z)
    // 堆索引
    var heapIdx = -1
    // 起点到当前节点的实际代价
    var actualCost = 0f
    // 估计总代价
    var totalCost = 0f
    // 代价
    var cost = 0f
    // 父节点
    var parent: Node? = null
    // 是否关闭
    var isClosed = false
    // 走过的距离
    var walkedDistance = 0f
    // 代价惩罚
    var costMalus = 0f
    // 节点类型
    var type = PathType.BLOCKED

    /**
     * a
     */
    fun cloneAndMove(x: Int, y: Int, z: Int): Node {
        return Node(x, y, z).also { node ->
            node.heapIdx = heapIdx
            node.actualCost = actualCost
            node.totalCost = totalCost
            node.cost = cost
            node.parent = parent
            node.isClosed = isClosed
            node.walkedDistance = walkedDistance
            node.costMalus = costMalus
            node.type = type
        }
    }

    /**
     * a
     */
    fun distanceTo(node: Node): Float {
        val deltaX = (node.x - x).toFloat()
        val deltaY = (node.y - y).toFloat()
        val deltaZ = (node.z - z).toFloat()
        return sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
    }

    /**
     * b
     */
    fun distanceToSqr(node: Node): Float {
        val deltaX = (node.x - x).toFloat()
        val deltaY = (node.y - y).toFloat()
        val deltaZ = (node.z - z).toFloat()
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
    }

    /**
     * c
     */
    fun distanceManhattan(node: Node): Float {
        val deltaX = abs(node.x - x).toFloat()
        val deltaY = abs(node.y - y).toFloat()
        val deltaZ = abs(node.z - z).toFloat()
        return deltaX + deltaY + deltaZ
    }

    /**
     * c
     */
    fun distanceManhattan(position: Vector): Float {
        val deltaX = abs(position.x - x).toFloat()
        val deltaY = abs(position.y - y).toFloat()
        val deltaZ = abs(position.z - z).toFloat()
        return deltaX + deltaY + deltaZ
    }

    fun asBlockPos(): Vector {
        return Vector(x, y, z)
    }

    fun inOpenSet(): Boolean {
        return heapIdx >= 0
    }

    fun display(world: World) {
        world.spawnParticle(Particle.FLAME, x + 0.5, y + 0.5, z + 0.5, 10, 0.0, 0.0, 0.0, 0.0)
    }

    fun display(player: Player) {
        player.spawnParticle(Particle.FLAME, x + 0.5, y + 0.5, z + 0.5, 10, 0.0, 0.0, 0.0, 0.0)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Node) {
            hash == other.hash && x == other.x && y == other.y && z == other.z
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return hash
    }

    override fun toString(): String {
        return "Node{x=$x, y=$y, z=$z, walk=${walkedDistance}, cost=${costMalus}}"
    }

    companion object {

        /** y 位宽（12 位，以补码低位表示，可无碰撞覆盖 -2048..2047，足以容纳任何现代世界高度） */
        private const val Y_BITS = 12

        /** x 位宽（10 位，即 x 每 1024 格循环一次） */
        private const val X_BITS = 10

        /** y 掩码 */
        private const val Y_MASK = (1 shl Y_BITS) - 1

        /** x / z 掩码 */
        private const val XZ_MASK = (1 shl X_BITS) - 1

        /** x 在哈希中的偏移量 */
        private const val X_SHIFT = Y_BITS

        /** z 在哈希中的偏移量 */
        private const val Z_SHIFT = Y_BITS + X_BITS

        /**
         * 计算节点坐标哈希。
         *
         * 位布局（低位到高位）：`[0,12) = y`、`[12,22) = x`、`[22,32) = z`。
         * 三段均取补码低位，因此负坐标与正坐标天然分离，无需额外符号标志位。
         *
         * 旧实现存在两类真实碰撞，均已消除：
         * 1. y 仅保留 8 位，1.18+ 的 -64..320 共 385 格必然重叠（如 y=-64 与 y=192）；
         * 2. `x shl 8` 的第 8 位与 `z < 0` 的 `0x8000` 标志位重叠（如 x=128,z=1 与 x=0,z=-32767）。
         *
         * 注意：32 位空间无法承载完整的 x/z 坐标范围，本函数仍是哈希而非唯一编码——
         * x 或 z 相差 1024 的整数倍、y 相差 4096 的整数倍时仍会碰撞。
         * 节点表 [NodeReader.nodes] 通过开放寻址探测（比对真实 x/y/z）来消除碰撞后果。
         *
         * 兼容性：[hash] 仍为公开的 `Int` 字段，签名与类型均未变化，仅数值分布改变。
         * 该值只用于同一次寻路过程中的临时节点表与 [hashCode]，不会被持久化，
         * 因此对外部调用方无实质影响。
         */
        fun createHash(x: Int, y: Int, z: Int): Int {
            return (y and Y_MASK) or ((x and XZ_MASK) shl X_SHIFT) or ((z and XZ_MASK) shl Z_SHIFT)
        }
    }
}