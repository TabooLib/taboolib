package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Path 通过反射构造，避免编译期直接引用 org.bukkit.util.Vector。
 * 运行时由 test/java 下的 Vector 桩类提供实现。
 *
 * @author sky
 */
class PathTest {

    private fun makePath(vararg coords: Triple<Int, Int, Int>, reached: Boolean = false): Path {
        val nodes = coords.map { (x, y, z) -> Node(x, y, z) }.toMutableList()
        val vectorClass = Class.forName("org.bukkit.util.Vector")
        val vectorCtor = vectorClass.getConstructor(Int::class.java, Int::class.java, Int::class.java)
        val last = coords.last()
        val target = vectorCtor.newInstance(last.first, last.second, last.third)
        val pathCtor = Path::class.java.getConstructor(MutableList::class.java, vectorClass, Boolean::class.javaPrimitiveType)
        return pathCtor.newInstance(nodes, target, reached) as Path
    }

    @Test
    fun `advance and hasNext`() {
        val path = makePath(Triple(0, 0, 0), Triple(1, 0, 0), Triple(2, 0, 0))
        assertTrue(path.hasNext())
        path.advance()
        path.advance()
        path.advance()
        assertFalse(path.hasNext())
        assertTrue(path.isDone())
    }

    @Test
    fun `getNode by index`() {
        val path = makePath(Triple(0, 0, 0), Triple(5, 10, 15))
        val node = path.getNode(1)
        assertEquals(5, node.x)
        assertEquals(10, node.y)
        assertEquals(15, node.z)
    }

    @Test
    fun `truncateNode reduces length`() {
        val path = makePath(Triple(0, 0, 0), Triple(1, 0, 0), Triple(2, 0, 0), Triple(3, 0, 0))
        assertEquals(4, path.getNodeCount())
        path.truncateNode(2)
        assertEquals(2, path.getNodeCount())
    }

    @Test
    fun `sameAs identical`() {
        val a = makePath(Triple(0, 0, 0), Triple(1, 1, 1))
        val b = makePath(Triple(0, 0, 0), Triple(1, 1, 1))
        assertTrue(a.sameAs(b))
    }

    @Test
    fun `sameAs different`() {
        val a = makePath(Triple(0, 0, 0), Triple(1, 1, 1))
        val b = makePath(Triple(0, 0, 0), Triple(2, 2, 2))
        assertFalse(a.sameAs(b))
    }

    @Test
    fun `sameAs null returns false`() {
        val a = makePath(Triple(0, 0, 0))
        assertFalse(a.sameAs(null))
    }

    @Test
    fun `canReach reflects reached flag`() {
        val reachable = makePath(Triple(0, 0, 0), reached = true)
        val unreachable = makePath(Triple(0, 0, 0), reached = false)
        assertTrue(reachable.canReach())
        assertFalse(unreachable.canReach())
    }
}
