package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

/**
 * @author sky
 */
class NodeTest {

    @Test
    fun `createHash uniqueness`() {
        val h1 = Node.createHash(1, 2, 3)
        val h2 = Node.createHash(4, 5, 6)
        assertNotEquals(h1, h2)
    }

    @Test
    fun `createHash negative flags`() {
        val positiveHash = Node.createHash(1, 0, 1)
        val negXHash = Node.createHash(-1, 0, 1)
        val negZHash = Node.createHash(1, 0, -1)
        // 负 x 设置最高位
        assertEquals(0, positiveHash and Int.MIN_VALUE)
        assertNotEquals(0, negXHash and Int.MIN_VALUE)
        // 负 z 设置 0x8000 位
        assertEquals(0, positiveHash and 0x8000)
        assertNotEquals(0, negZHash and 0x8000)
    }

    @Test
    fun `createHash no collision for small positive range`() {
        val hashes = mutableSetOf<Int>()
        for (x in 0..50) {
            for (z in 0..50) {
                val h = Node.createHash(x, 64, z)
                assertTrue(hashes.add(h), "Hash 碰撞: x=$x, z=$z")
            }
        }
    }

    @Test
    fun `distanceTo euclidean`() {
        val a = Node(0, 0, 0)
        val b = Node(3, 4, 0)
        val expected = sqrt(9.0 + 16.0).toFloat()
        assertEquals(expected, a.distanceTo(b), 0.001f)
    }

    @Test
    fun `distanceManhattan between nodes`() {
        val a = Node(1, 2, 3)
        val b = Node(4, 6, 1)
        // |4-1| + |6-2| + |1-3| = 3 + 4 + 2 = 9
        assertEquals(9f, a.distanceManhattan(b))
    }

    @Test
    fun `equals same coordinates`() {
        val a = Node(10, 20, 30)
        val b = Node(10, 20, 30)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `equals different coordinates`() {
        val a = Node(10, 20, 30)
        val b = Node(10, 20, 31)
        assertNotEquals(a, b)
    }

    @Test
    fun `cloneAndMove produces independent node with same properties`() {
        val original = Node(1, 2, 3)
        original.actualCost = 5f
        original.totalCost = 10f
        original.cost = 3f
        original.isClosed = true
        original.walkedDistance = 7f
        original.costMalus = 2f
        original.type = PathType.OPEN
        val cloned = original.cloneAndMove(10, 20, 30)
        // 坐标独立
        assertEquals(10, cloned.x)
        assertEquals(20, cloned.y)
        assertEquals(30, cloned.z)
        // 属性复制
        assertEquals(original.actualCost, cloned.actualCost)
        assertEquals(original.totalCost, cloned.totalCost)
        assertEquals(original.cost, cloned.cost)
        assertEquals(original.isClosed, cloned.isClosed)
        assertEquals(original.walkedDistance, cloned.walkedDistance)
        assertEquals(original.costMalus, cloned.costMalus)
        assertEquals(original.type, cloned.type)
    }
}
