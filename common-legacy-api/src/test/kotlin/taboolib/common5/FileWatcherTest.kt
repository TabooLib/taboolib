package taboolib.common5

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FileWatcherTest {

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `file deletion is reported and watcher can be released repeatedly`() {
        val file = Files.write(tempDirectory.resolve("watched.txt"), byteArrayOf(1)).toFile()
        val deleted = CountDownLatch(1)
        val watcher = FileWatcher(20)
        try {
            watcher.addSimpleListener(file, { changed ->
                if (changed.absoluteFile == file.absoluteFile && !changed.exists()) {
                    deleted.countDown()
                }
            })
            Files.delete(file.toPath())

            assertTrue(deleted.await(5, TimeUnit.SECONDS))
        } finally {
            watcher.release()
            watcher.release()
            FileWatcher.INSTANCE.release()
        }
    }
}
