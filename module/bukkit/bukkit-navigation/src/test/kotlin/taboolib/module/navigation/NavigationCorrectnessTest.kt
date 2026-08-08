package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavigationCorrectnessTest {

    @Test
    fun `world height bounds include negative build height and exclude max height`() {
        assertFalse(isWithinNavigationHeight(-65, -64, 320))
        assertTrue(isWithinNavigationHeight(-64, -64, 320))
        assertTrue(isWithinNavigationHeight(319, -64, 320))
        assertFalse(isWithinNavigationHeight(320, -64, 320))
    }

    @Test
    fun `node cache keeps distinct nodes across modern world heights`() {
        val nodes = HashMap<Int, Node>()
        // 旧哈希 y 只有 8 位，-64 与 192 必然碰撞；新哈希 y 为 12 位，两者不再相同
        assertNotEquals(Node.createHash(4, -64, 8), Node.createHash(4, 192, 8))

        val low = getOrCreateNavigationNode(nodes, 4, -64, 8)
        val high = getOrCreateNavigationNode(nodes, 4, 192, 8)

        assertNotSame(low, high)
        assertEquals(-64, low.y)
        assertEquals(192, high.y)
        assertSame(low, getOrCreateNavigationNode(nodes, 4, -64, 8))
        assertSame(high, getOrCreateNavigationNode(nodes, 4, 192, 8))
    }

    @Test
    fun `node cache resolves residual hash collisions by open addressing`() {
        val nodes = HashMap<Int, Node>()
        // x 仅编码 10 位，x 与 x+1024 哈希相同，靠开放寻址区分
        assertEquals(Node.createHash(0, 64, 0), Node.createHash(1024, 64, 0))

        val first = getOrCreateNavigationNode(nodes, 0, 64, 0)
        val second = getOrCreateNavigationNode(nodes, 1024, 64, 0)

        assertNotSame(first, second)
        assertEquals(0, first.x)
        assertEquals(1024, second.x)
        assertSame(first, getOrCreateNavigationNode(nodes, 0, 64, 0))
        assertSame(second, getOrCreateNavigationNode(nodes, 1024, 64, 0))
    }

    @Test
    fun `surface selection only rejects water when water is disabled`() {
        assertTrue(RandomPositionGenerator.acceptsNavigationSurface(false, false))
        assertFalse(RandomPositionGenerator.acceptsNavigationSurface(false, true))
        assertTrue(RandomPositionGenerator.acceptsNavigationSurface(true, false))
        assertTrue(RandomPositionGenerator.acceptsNavigationSurface(true, true))
    }

    @Test
    fun `fluid categories keep water and lava distinct`() {
        assertTrue(Fluid.WATER.isWater())
        assertTrue(Fluid.FLOWING_WATER.isWater())
        assertFalse(Fluid.LAVA.isWater())
        assertFalse(Fluid.FLOWING_LAVA.isWater())
        assertTrue(Fluid.LAVA.isLava())
        assertTrue(Fluid.FLOWING_LAVA.isLava())
        assertFalse(Fluid.WATER.isLava())
    }

    @Test
    fun `path smoothing rejects unsupported liquid dangerous and blocked cells`() {
        assertTrue(PathSmoothing.isSafeSmoothingFeetType(PathType.WALKABLE, 0.0f))
        assertFalse(PathSmoothing.isSafeSmoothingFeetType(PathType.OPEN, 0.0f))
        assertFalse(PathSmoothing.isSafeSmoothingFeetType(PathType.WATER, PathType.WATER.malus))
        assertFalse(PathSmoothing.isSafeSmoothingFeetType(PathType.LAVA, PathType.LAVA.malus))
        assertFalse(PathSmoothing.isSafeSmoothingFeetType(PathType.DANGER_FIRE, PathType.DANGER_FIRE.malus))

        assertTrue(PathSmoothing.isSafeSmoothingBodyType(PathType.OPEN.malus))
        assertFalse(PathSmoothing.isSafeSmoothingBodyType(PathType.WATER.malus))
        assertFalse(PathSmoothing.isSafeSmoothingBodyType(PathType.DAMAGE_FIRE.malus))
        assertFalse(PathSmoothing.isSafeSmoothingBodyType(PathType.BLOCKED.malus))
    }

    @Test
    fun `path smoothing accepts partial blocks as support`() {
        // 满方块：顶面恰好等于脚部高度
        assertTrue(PathSmoothing.isSupportedAtHeight(64.0, 64.0))
        // 半砖（0.5）、农田 / 雪层（0.9375）：顶面略低于脚部，仍应视为可站立
        assertTrue(PathSmoothing.isSupportedAtHeight(63.5, 64.0))
        assertTrue(PathSmoothing.isSupportedAtHeight(63.9375, 64.0))
        // 空气 / 非实心方块（getBlockHeight 返回 0.0）：顶面等于脚下方块底面，无支撑
        assertFalse(PathSmoothing.isSupportedAtHeight(63.0, 64.0))
        // 顶面显著高于脚部：实体被埋在方块里
        assertFalse(PathSmoothing.isSupportedAtHeight(65.0, 64.0))
    }
}
