package taboolib.module.navigation

import com.google.common.collect.Lists
import org.bukkit.Location
import org.bukkit.util.Vector

/**
 * Navigation
 * taboolib.module.navigation.PathFinder
 *
 * @author sky
 * @since 2021/2/21 9:46 下午
 */
class PathFinder(val nodeReader: NodeReader) {

    var neighbors = arrayOfNulls<Node>(32)
    val openSet = BinaryHeap()

    fun findPath(position: Location, distance: Float, distanceManhattan: Int = 1, deep: Float = 1f): Path? {
        return findPath(setOf(position.toVector()), distance, distanceManhattan, deep)
    }

    fun findPath(position: Vector, distance: Float, distanceManhattan: Int = 1, deep: Float = 1f): Path? {
        return findPath(setOf(position), distance, distanceManhattan, deep)
    }

    fun findPath(position: Set<Vector>, distance: Float, distanceManhattan: Int = 1, deep: Float = 1f): Path? {
        openSet.clear()
        val start = nodeReader.getStart()
        val map = position.map {
            nodeReader.getGoal(it.x, it.y, it.z) to it
        }
        return findPath(start, map, distance, distanceManhattan, deep).also {
            nodeReader.done()
        }
    }

    private fun findPath(begin: Node, list: List<Pair<NodeTarget, Vector>>, distance: Float, distanceManhattan: Int, deep: Float): Path? {
        begin.g = 0.0f
        begin.f = getBestHeuristic(begin, list)
        begin.cost = begin.f
        openSet.clear()
        openSet.insert(begin)
        var heap = 0
        val set = Lists.newArrayListWithExpectedSize<Pair<NodeTarget, Vector>>(list.size)
        val maxHeap = (distance * 16 * deep).toInt()
        while (!openSet.isEmpty()) {
            ++heap
            if (heap >= maxHeap) {
                break
            }
            val pop = openSet.pop()
            pop.closed = true
            var neighbors = 0
            while (neighbors < list.size) {
                val entry = list[neighbors]
                val target = entry.first
                if (pop.distanceManhattan(target) <= distanceManhattan.toFloat()) {
                    target.setReached()
                    set.add(entry)
                }
                ++neighbors
            }
            if (set.isNotEmpty()) {
                break
            }
            if (pop.distanceTo(begin) < distance) {
                neighbors = nodeReader.getNeighbors(this.neighbors, pop)
                for (index in 0 until neighbors) {
                    val node2 = this.neighbors[index]!!
                    val f2 = pop.distanceTo(node2)
                    node2.walkedDistance = pop.walkedDistance + f2
                    val f3 = pop.cost + f2 + node2.costMalus
                    if (node2.walkedDistance < distance && (!node2.inOpenSet() || f3 < node2.g)) {
                        node2.cameFrom = pop
                        node2.cost = f3
                        node2.f = getBestHeuristic(node2, list) * 1.5f
                        if (node2.inOpenSet()) {
                            openSet.changeCost(node2, node2.cost + node2.f)
                        } else {
                            node2.g = node2.cost + node2.f
                            openSet.insert(node2)
                        }
                    }
                }
            }
        }
        var best: Path? = null
        val useSet1 = set.isEmpty()
        val comparator = if (useSet1) {
            Comparator.comparingInt { p: Path -> p.getNodeCount() }
        } else {
            Comparator.comparingDouble { p: Path -> p.distToTarget.toDouble() }.thenComparingInt { obj: Path -> obj.getNodeCount() }
        }
        val iterator = (if (useSet1) list else set).iterator()
        while (true) {
            var path: Path?
            do {
                if (!iterator.hasNext()) {
                    return best
                }
                val entry = iterator.next()
                path = reconstructPath(entry.first.bestNode!!, entry.second, !useSet1)
            } while (best != null && comparator.compare(path, best) >= 0)
            best = path
        }
    }

    private fun getBestHeuristic(node: Node, list: List<Pair<NodeTarget, Vector>>): Float {
        var bestH = 3.4028235E38f
        var i = 0
        val length = list.size
        while (i < length) {
            val target = list[i].first
            val bestHeuristic = node.distanceTo(target)
            target.updateBest(bestHeuristic, node)
            bestH = bestHeuristic.coerceAtMost(bestH)
            ++i
        }
        return bestH
    }

    private fun reconstructPath(node: Node, position: Vector, flag: Boolean): Path {
        val list = Lists.newArrayList<Node>()
        var node0 = node
        list.add(0, node)
        while (node0.cameFrom != null) {
            node0 = node0.cameFrom!!
            list.add(0, node0)
        }
        return Path(list, position, flag)
    }
}