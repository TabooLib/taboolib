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
        world.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y + 0.5, z + 0.5, 10, 0.0, 0.0, 0.0, 0.0)
    }

    fun display(player: Player) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, x + 0.5, y + 0.5, z + 0.5, 10, 0.0, 0.0, 0.0, 0.0)
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

        fun createHash(x: Int, y: Int, z: Int): Int {
            return y and 255 or (x and 32767 shl 8) or (z and 32767 shl 24) or (if (x < 0) -2147483648 else 0) or (if (z < 0) '耀'.code else 0)
        }
    }
}