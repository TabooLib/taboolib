package taboolib.common.function

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThrottleTest {

    @Test
    fun `first invocation is allowed for any delay`() {
        val throttle = throttle(Long.MAX_VALUE)

        assertTrue(throttle.canExecute())
        assertFalse(throttle.canExecute())
    }

    @Test
    fun `non-positive delay never suppresses invocation`() {
        val throttle = throttle(0)

        repeat(10) {
            assertTrue(throttle.canExecute())
        }
    }

    @Test
    fun `keyed throttle treats each key independently`() {
        val throttle = throttle<String>(Long.MAX_VALUE)

        assertTrue(throttle.canExecute("first"))
        assertFalse(throttle.canExecute("first"))
        assertTrue(throttle.canExecute("second"))
    }
}
