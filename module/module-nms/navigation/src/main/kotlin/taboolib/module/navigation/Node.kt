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

    // m
    val hash = createHash(x, y, z)
    // d
    var heapIdx = -1
    // e
    var g = 0f
    // f
    var f = 0f
    // g
    var cost = 0f
    // h
    var cameFrom: Node? = null
    // i
    var closed = false
    // j
    var walkedDistance = 0f
    // k
    var costMalus = 0f
    // l
    var type = PathType.BLOCKED

    /**
     * a
     */
    fun cloneAndMove(x: Int, y: Int, z: Int): Node {
        return Node(x, y, z).also { node ->
            node.heapIdx = heapIdx
            node.g = g
            node.f = f
            node.cost = cost
            node.cameFrom = cameFrom
            node.closed = closed
            node.walkedDistance = walkedDistance
            node.costMalus = costMalus
            node.type = type
        }
    }

    /**
     * a
     */
    fun distanceTo(node: Node): Float {
        val f = (node.x - x).toFloat()
        val f1 = (node.y - y).toFloat()
        val f2 = (node.z - z).toFloat()
        return sqrt((f * f + f1 * f1 + f2 * f2).toDouble()).toFloat()
    }

    /**
     * b
     */
    fun distanceToSqr(node: Node): Float {
        val f = (node.x - x).toFloat()
        val f1 = (node.y - y).toFloat()
        val f2 = (node.z - z).toFloat()
        return f * f + f1 * f1 + f2 * f2
    }

    /**
     * c
     */
    fun distanceManhattan(node: Node): Float {
        val f = abs(node.x - x).toFloat()
        val f1 = abs(node.y - y).toFloat()
        val f2 = abs(node.z - z).toFloat()
        return f + f1 + f2
    }

    /**
     * c
     */
    fun distanceManhattan(position: Vector): Float {
        val f = abs(position.x - x).toFloat()
        val f1 = abs(position.y - y).toFloat()
        val f2 = abs(position.z - z).toFloat()
        return f + f1 + f2
    }

    fun asBlockPos(): Vector {
        return Vector(x, y, z)
    }

    fun inOpenSet(): Boolean {
        return heapIdx >= 0
    }

    fun display(world: World) {
        world.spawnParticle(Particle.VILLAGER_HAPPY, 0.0, 0.0, 0.0, 10, x + 0.5, y + 0.5, z + 0.5)
    }

    fun display(player: Player) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, 0.0, 0.0, 0.0, 10, x + 0.5, y + 0.5, z + 0.5)
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
            return y and 255 or (x and 32767 shl 8) or (z and 32767 shl 24) or (if (x < 0) -2147483648 else 0) or (if (z < 0) '耀'.toInt() else 0)
        }
    }
}