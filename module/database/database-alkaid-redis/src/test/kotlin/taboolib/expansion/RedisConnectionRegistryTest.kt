package taboolib.expansion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger

class RedisConnectionRegistryTest {

    @Test
    fun `registered connections close exactly once`() {
        val registry = RedisConnectionRegistry()
        val closeCount = AtomicInteger()
        val connection = Closeable { closeCount.incrementAndGet() }

        registry.register(connection)
        registry.register(connection)
        registry.closeAll()
        registry.closeAll()

        assertEquals(1, closeCount.get())
        assertEquals(0, registry.size())
    }

    @Test
    fun `unregistered connection remains caller owned`() {
        val registry = RedisConnectionRegistry()
        val closeCount = AtomicInteger()
        val connection = Closeable { closeCount.incrementAndGet() }

        registry.register(connection)
        registry.unregister(connection)
        registry.closeAll()

        assertEquals(0, closeCount.get())
    }

    @Test
    fun `connection registered after shutdown closes immediately`() {
        val registry = RedisConnectionRegistry()
        val closeCount = AtomicInteger()

        registry.closeAll()
        registry.register(Closeable { closeCount.incrementAndGet() })

        assertEquals(1, closeCount.get())
        assertEquals(0, registry.size())
    }
}
