package taboolib.expansion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taboolib.library.configuration.ConfigurationSection
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicInteger

class LettuceRedisResourceRegistryTest {

    @Test
    fun `registered clients stop exactly once`() {
        val registry = LettuceRedisResourceRegistry()
        val stopCount = AtomicInteger()
        val resource = LettuceRedisResource { stopCount.incrementAndGet() }

        registry.register(resource)
        registry.register(resource)
        registry.closeAll()
        registry.closeAll()

        assertEquals(1, stopCount.get())
        assertEquals(0, registry.size())
    }

    @Test
    fun `unregistered client remains caller managed`() {
        val registry = LettuceRedisResourceRegistry()
        val stopCount = AtomicInteger()
        val resource = LettuceRedisResource { stopCount.incrementAndGet() }

        registry.register(resource)
        registry.unregister(resource)
        registry.closeAll()

        assertEquals(0, stopCount.get())
    }

    @Test
    fun `client registered after shutdown stops immediately`() {
        val registry = LettuceRedisResourceRegistry()
        val stopCount = AtomicInteger()

        registry.closeAll()
        registry.register(LettuceRedisResource { stopCount.incrementAndGet() })

        assertEquals(1, stopCount.get())
        assertEquals(0, registry.size())
    }

    @Test
    fun `startup coordinator reports the first failure immediately`() {
        val successCount = AtomicInteger()
        val failureCount = AtomicInteger()
        val settledCount = AtomicInteger()
        val coordinator = AsyncStartupCoordinator(
            stageCount = 2,
            onSuccess = { successCount.incrementAndGet() },
            onFailure = { failureCount.incrementAndGet() },
            onSettled = { settledCount.incrementAndGet() },
        )

        coordinator.complete(IllegalStateException("failed"))

        assertEquals(0, successCount.get())
        assertEquals(1, failureCount.get())
        assertEquals(1, settledCount.get())

        coordinator.complete(null)

        assertEquals(0, successCount.get())
        assertEquals(1, failureCount.get())
        assertEquals(2, settledCount.get())
    }

    @Test
    fun `startup coordinator succeeds after every stage settles`() {
        val successCount = AtomicInteger()
        val failureCount = AtomicInteger()
        val coordinator = AsyncStartupCoordinator(
            stageCount = 2,
            onSuccess = { successCount.incrementAndGet() },
            onFailure = { failureCount.incrementAndGet() },
            onSettled = {},
        )

        coordinator.complete(null)
        assertEquals(0, successCount.get())

        coordinator.complete(null)
        assertEquals(1, successCount.get())
        assertEquals(0, failureCount.get())
    }

    @Test
    fun `synchronous start after stop throws`() {
        val client = LettuceRedisClient(testConfig())
        client.stop()

        assertThrows(IllegalStateException::class.java) {
            client.startSync()
        }
    }

    @Test
    fun `asynchronous start after stop returns failed future`() {
        val client = LettuceRedisClient(testConfig())
        client.stop()

        assertTrue(client.start().isCompletedExceptionally)
    }

    private fun testConfig(): LettuceRedisConfig {
        val configuration = Proxy.newProxyInstance(
            ConfigurationSection::class.java.classLoader,
            arrayOf(ConfigurationSection::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getString" -> when (args?.getOrNull(0)) {
                    "host" -> "127.0.0.1"
                    "timeout" -> "1s"
                    else -> args?.getOrNull(1)
                }
                "getInt" -> args?.getOrNull(1) ?: 0
                "getBoolean" -> args?.getOrNull(1) ?: false
                "getConfigurationSection" -> null
                "getKeys" -> emptySet<String>()
                "getStringList", "getEnumList" -> emptyList<Any>()
                "contains" -> false
                else -> null
            }
        } as ConfigurationSection
        return LettuceRedisConfig(configuration)
    }
}
