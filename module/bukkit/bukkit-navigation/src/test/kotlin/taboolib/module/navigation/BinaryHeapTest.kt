package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author sky
 */
class BinaryHeapTest {

    private lateinit var heap: BinaryHeap

    @BeforeEach
    fun setup() {
        heap = BinaryHeap()
    }

    @Test
    fun `insert and pop order`() {
        val costs = floatArrayOf(5f, 1f, 3f, 2f, 4f)
        for ((i, c) in costs.withIndex()) {
            val node = Node(i, 0, 0)
            node.actualCost = c
            heap.insert(node)
        }
        var prev = Float.MIN_VALUE
        while (!heap.isEmpty()) {
            val popped = heap.pop()
            assertTrue(popped.actualCost >= prev)
            prev = popped.actualCost
        }
    }

    @Test
    fun `changeCost upward`() {
        val a = Node(0, 0, 0).also { it.actualCost = 10f }
        val b = Node(1, 0, 0).also { it.actualCost = 5f }
        val c = Node(2, 0, 0).also { it.actualCost = 8f }
        heap.insert(a)
        heap.insert(b)
        heap.insert(c)
        // 降低 a 的代价，使其成为最小
        heap.changeCost(a, 1f)
        assertEquals(a, heap.pop())
        assertEquals(b, heap.pop())
        assertEquals(c, heap.pop())
    }

    @Test
    fun `changeCost downward`() {
        val a = Node(0, 0, 0).also { it.actualCost = 1f }
        val b = Node(1, 0, 0).also { it.actualCost = 5f }
        val c = Node(2, 0, 0).also { it.actualCost = 8f }
        heap.insert(a)
        heap.insert(b)
        heap.insert(c)
        // 增大 a 的代价，使其不再是最小
        heap.changeCost(a, 10f)
        assertEquals(b, heap.pop())
        assertEquals(c, heap.pop())
        assertEquals(a, heap.pop())
    }

    @Test
    fun `changeCost does not overwrite node cost`() {
        val node = Node(0, 0, 0)
        node.actualCost = 5f
        node.cost = 42f
        heap.insert(node)
        heap.changeCost(node, 2f)
        assertEquals(42f, node.cost, "changeCost 不应修改 node.cost (g 值)")
        assertEquals(2f, node.actualCost)
    }

    @Test
    fun `dynamic growth beyond initial capacity`() {
        for (i in 0 until 200) {
            val node = Node(i, 0, 0)
            node.actualCost = i.toFloat()
            heap.insert(node)
        }
        assertEquals(200, heap.size)
        for (i in 0 until 200) {
            val popped = heap.pop()
            assertEquals(i.toFloat(), popped.actualCost)
        }
    }

    @Test
    fun `insert duplicate throws`() {
        val node = Node(0, 0, 0)
        heap.insert(node)
        assertThrows(IllegalStateException::class.java) {
            heap.insert(node)
        }
    }

    @Test
    fun `large scale ordering`() {
        val random = java.util.Random(12345)
        val nodes = (0 until 1000).map { i ->
            Node(i, 0, 0).also { it.actualCost = random.nextFloat() * 10000 }
        }
        for (node in nodes) {
            heap.insert(node)
        }
        var prev = Float.MIN_VALUE
        while (!heap.isEmpty()) {
            val popped = heap.pop()
            assertTrue(popped.actualCost >= prev, "Pop 序列应单调递增")
            prev = popped.actualCost
        }
    }
}
