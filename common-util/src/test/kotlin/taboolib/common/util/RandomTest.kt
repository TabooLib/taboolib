package taboolib.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RandomTest {

    @Test
    fun `zero probability is always false`() {
        repeat(10_000) {
            assertFalse(random(0.0))
        }
    }

    @Test
    fun `probability at least one is always true`() {
        repeat(100) {
            assertTrue(random(1.0))
            assertTrue(random(Double.POSITIVE_INFINITY))
        }
    }

    @Test
    fun `inclusive int range supports full integer domain`() {
        repeat(10_000) {
            val value = random(Int.MIN_VALUE, Int.MAX_VALUE)
            assertTrue(value in Int.MIN_VALUE..Int.MAX_VALUE)
        }
    }

    @Test
    fun `equal maximum bounds return maximum value`() {
        assertEquals(Int.MAX_VALUE, random(Int.MAX_VALUE, Int.MAX_VALUE))
    }
}
