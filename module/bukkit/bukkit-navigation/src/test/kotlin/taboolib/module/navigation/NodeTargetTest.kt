package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author sky
 */
class NodeTargetTest {

    @Test
    fun `updateBest improves with smaller heuristic`() {
        val target = NodeTarget(Node(0, 0, 0))
        val nodeA = Node(1, 0, 0)
        val nodeB = Node(2, 0, 0)
        target.updateBest(10f, nodeA)
        assertEquals(nodeA, target.bestNode)
        assertEquals(10f, target.bestHeuristic)
        target.updateBest(5f, nodeB)
        assertEquals(nodeB, target.bestNode)
        assertEquals(5f, target.bestHeuristic)
    }

    @Test
    fun `updateBest ignores worse heuristic`() {
        val target = NodeTarget(Node(0, 0, 0))
        val nodeA = Node(1, 0, 0)
        val nodeB = Node(2, 0, 0)
        target.updateBest(5f, nodeA)
        target.updateBest(10f, nodeB)
        assertEquals(nodeA, target.bestNode)
        assertEquals(5f, target.bestHeuristic)
    }

    @Test
    fun `setReached toggles state`() {
        val target = NodeTarget(Node(0, 0, 0))
        assertFalse(target.reached)
        target.setReached()
        assertTrue(target.reached)
    }
}
