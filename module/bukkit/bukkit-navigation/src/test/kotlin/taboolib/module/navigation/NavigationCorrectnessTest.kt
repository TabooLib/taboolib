package taboolib.module.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    fun `node cache resolves legacy hash collisions across modern world heights`() {
        val nodes = HashMap<Int, Node>()
        assertEquals(Node.createHash(4, -64, 8), Node.createHash(4, 192, 8))

        val low = getOrCreateNavigationNode(nodes, 4, -64, 8)
        val high = getOrCreateNavigationNode(nodes, 4, 192, 8)

        assertNotSame(low, high)
        assertEquals(-64, low.y)
        assertEquals(192, high.y)
        assertSame(low, getOrCreateNavigationNode(nodes, 4, -64, 8))
        assertSame(high, getOrCreateNavigationNode(nodes, 4, 192, 8))
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
}
