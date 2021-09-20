package taboolib.module.navigation

/**
 * Navigation
 * taboolib.module.navigation.Target
 * net.minecraft.world.level.pathfinder.Target
 *
 * @author sky
 * @since 2021/2/21 6:29 下午
 */
class NodeTarget(node: Node) : Node(node.x, node.y, node.z) {

    var bestHeuristic = 3.4028235E38f
        private set
    // d()
    var bestNode: Node? = null
        private set
    var reached = false

    fun updateBest(bestHeuristic: Float, node: Node?) {
        if (bestHeuristic < this.bestHeuristic) {
            this.bestHeuristic = bestHeuristic
            bestNode = node
        }
    }

    // e
    fun setReached() {
        reached = true
    }
}