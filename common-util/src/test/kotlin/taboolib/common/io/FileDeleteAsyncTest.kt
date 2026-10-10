package taboolib.common.io

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class FileDeleteAsyncTest {

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `large directory tree completes without executor starvation`() {
        val root = Files.createDirectory(tempDirectory.resolve("root"))
        val branchCount = maxOf(16, Runtime.getRuntime().availableProcessors() * 2)
        repeat(branchCount) { index ->
            val branch = Files.createDirectory(root.resolve("branch-$index"))
            Files.write(branch.resolve("value.txt"), index.toString().toByteArray())
        }
        val futures = Collections.synchronizedSet(mutableSetOf<Future<*>>())

        root.toFile().deepDeleteAsync(futures = futures)

        futures.single().get(10, TimeUnit.SECONDS)
        assertFalse(Files.exists(root))
    }

    @Test
    fun `missing path is a successful no-op`() {
        val missing = tempDirectory.resolve("missing").toFile()
        val futures = mutableSetOf<Future<*>>()

        missing.deepDeleteAsync(futures = futures)

        futures.single().get(5, TimeUnit.SECONDS)
        assertFalse(missing.exists())
    }
}
