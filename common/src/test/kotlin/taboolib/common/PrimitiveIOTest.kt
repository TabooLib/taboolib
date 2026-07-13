package taboolib.common

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.nio.file.Files
import java.nio.file.Path

class PrimitiveIOTest {

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `download closes input after success`() {
        val content = "taboolib".toByteArray()
        val input = TrackingInputStream(content)
        val target = tempDirectory.resolve("download.bin").toFile()

        PrimitiveIO.downloadFile(memoryUrl(input), target)

        assertTrue(input.closed)
        assertArrayEquals(content, Files.readAllBytes(target.toPath()))
    }

    @Test
    fun `download closes input when output cannot be opened`() {
        val input = TrackingInputStream("taboolib".toByteArray())
        val targetDirectory = Files.createDirectory(tempDirectory.resolve("target")).toFile()

        assertThrows<IOException> {
            PrimitiveIO.downloadFile(memoryUrl(input), targetDirectory)
        }
        assertTrue(input.closed)
    }

    private fun memoryUrl(input: TrackingInputStream): URL {
        return URL(null, "memory://download", object : URLStreamHandler() {
            override fun openConnection(url: URL): URLConnection {
                return object : URLConnection(url) {
                    override fun connect() = Unit
                    override fun getInputStream() = input
                }
            }
        })
    }

    private class TrackingInputStream(content: ByteArray) : ByteArrayInputStream(content) {

        var closed = false
            private set

        override fun close() {
            closed = true
            super.close()
        }
    }
}
