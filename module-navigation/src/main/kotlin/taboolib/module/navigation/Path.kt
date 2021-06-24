package taboolib.module.navigation

import org.bukkit.util.Vector

/**
 * Navigation
 * taboolib.module.navigation.Path
 *
 * @author sky
 * @since 2021/2/21 10:07 下午
 */
@Suppress("CanBeParameter")
class Path(val nodes: MutableList<Node>, val target: Vector, var reached: Boolean) {

    val openSet = arrayOfNulls<Node>(0)
    val closeSet = arrayOfNulls<Node>(0)
    var distToTarget = if (nodes.isEmpty()) 3.4028235E38f else nodes[nodes.size - 1].distanceManhattan(target)
    var nextNodeIndex = 0

    fun getNextIndex(): Int {
        return nextNodeIndex
    }

    fun hasNext(): Boolean {
        return getNextIndex() < nodes.size
    }

    fun advance() {
        ++nextNodeIndex
    }

    fun notStarted(): Boolean {
        return nextNodeIndex <= 0
    }

    fun isDone(): Boolean {
        return nextNodeIndex >= nodes.size
    }

    fun getFinalPoint(): Node? {
        return this.getEndNode()
    }

    fun getEndNode(): Node? {
        return if (nodes.isNotEmpty()) nodes[nodes.size - 1] else null
    }

    fun getNode(i: Int): Node {
        return nodes[i]
    }

    fun truncateNode(i: Int) {
        if (nodes.size > i) {
            nodes.subList(i, nodes.size).clear()
        }
    }

    fun replaceNode(i: Int, node: Node) {
        nodes[i] = node
    }

    fun getNodeCount(): Int {
        return nodes.size
    }

    fun getEntityPosAtNode(nodeEntity: NodeEntity, i: Int): Vector {
        val node = nodes[i]
        val x = node.x.toDouble() + (nodeEntity.width + 1.0f).toInt().toDouble() * 0.5
        val y = node.y.toDouble()
        val z = node.z.toDouble() + (nodeEntity.width + 1.0f).toInt().toDouble() * 0.5
        return Vector(x, y, z)
    }

    fun getNodePos(i: Int): Vector {
        return nodes[i].asBlockPos()
    }

    fun getNextEntityPos(entity: NodeEntity): Vector {
        return getEntityPosAtNode(entity, nextNodeIndex)
    }

    fun getNextNodePos(): Vector {
        return nodes[nextNodeIndex].asBlockPos()
    }

    fun getNextNode(): Node {
        return nodes[nextNodeIndex]
    }

    fun getPreviousNode(): Node? {
        return if (nextNodeIndex > 0) nodes[nextNodeIndex - 1] else null
    }

    fun sameAs(path: Path?): Boolean {
        return when {
            path == null -> {
                false
            }
            path.nodes.size != nodes.size -> {
                false
            }
            else -> {
                for (i in nodes.indices) {
                    val node0 = nodes[i]
                    val node1 = path.nodes[i]
                    if (node0.x != node1.x || node0.y != node1.y || node0.z != node1.z) {
                        return false
                    }
                }
                true
            }
        }
    }

    fun canReach(): Boolean {
        return reached
    }

    override fun toString(): String {
        return "Path(length=${nodes.size})"
    }
}