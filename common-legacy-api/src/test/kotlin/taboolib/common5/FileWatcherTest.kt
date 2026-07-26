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
                // Windows / macOS 上 absolutePath 与 canonicalPath 可能不等（8.3 短名、符号链接），
                // 因此统一取 canonicalFile 比较；文件已删除时 Files.isSameFile 会失败，故不使用它。
                if (changed.canonicalFile == file.canonicalFile && !changed.exists()) {
                    deleted.countDown()
                }
            })
            Files.delete(file.toPath())

            assertTrue(deleted.await(5, TimeUnit.SECONDS))
        } finally {
            // 只释放本用例创建的实例。
            // FileWatcher.INSTANCE 是全局单例且 released 不可逆，
            // 在此释放会污染同一 JVM 内的后续测试。
            watcher.release()
            watcher.release()
        }
    }

    @Test
    fun `listeners on the same directory do not cancel each other`() {
        val first = Files.write(tempDirectory.resolve("first.txt"), byteArrayOf(1)).toFile()
        val second = Files.write(tempDirectory.resolve("second.txt"), byteArrayOf(1)).toFile()
        val secondChanged = CountDownLatch(1)
        val watcher = FileWatcher(20)
        try {
            // Path.register 对同一目录返回同一个 WatchKey，
            // 移除其中一个监听器不应让同目录下其余监听器失效。
            watcher.addSimpleListener(first, {})
            watcher.addSimpleListener(second, { secondChanged.countDown() })
            watcher.removeListener(first)

            Files.write(second.toPath(), byteArrayOf(2))

            assertTrue(secondChanged.await(5, TimeUnit.SECONDS))
        } finally {
            watcher.release()
        }
    }
}
