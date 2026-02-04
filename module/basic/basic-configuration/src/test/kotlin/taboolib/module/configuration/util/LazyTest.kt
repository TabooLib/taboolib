package taboolib.module.configuration.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import taboolib.module.configuration.Configuration
import java.io.File

class LazyTest {

    @Test
    fun keyedLazyCachesUntilKeyChanges() {
        class Holder {
            var key: Int = 0
            var initCount: Int = 0
            val value: Int by KeyedLazy({ key }) { ++initCount }
        }

        val holder = Holder()
        assertEquals(1, holder.value)
        assertEquals(1, holder.value)

        holder.key = 1
        assertEquals(2, holder.value)
        assertEquals(2, holder.value)
    }

    @Test
    fun reloadAwareLazyInvalidatesWhenConfigReloads() {
        val config = Configuration.loadFromString("a: 1")
        var initCount = 0

        val value: Int by ReloadAwareLazy(config) { ++initCount }

        assertEquals(1, value)
        assertEquals(1, value)

        config.loadFromString("a: 2")
        assertEquals(2, value)
        assertEquals(2, value)
    }

    @Test
    fun reloadAwareLazyInvalidatesWhenFileFingerprintChanges(@TempDir tempDir: File) {
        val file = File(tempDir, "config.txt").apply { writeText("1") }
        var initCount = 0

        val value: Int by ReloadAwareLazy(file) { ++initCount }

        val firstLength = file.length()
        assertEquals(1, value)
        assertEquals(1, value)

        file.writeText("11")
        assertEquals(firstLength + 1, file.length())
        assertEquals(2, value)
        assertEquals(2, value)
    }
}
