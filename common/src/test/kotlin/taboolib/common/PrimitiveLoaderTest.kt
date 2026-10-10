package taboolib.common

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class PrimitiveLoaderTest {

    @TempDir
    lateinit var tempDirectory: Path

    @Test
    fun `missing and empty relocation targets are regenerated`() {
        val missing = tempDirectory.resolve("missing.jar").toFile()
        val empty = Files.createFile(tempDirectory.resolve("empty.jar")).toFile()

        assertTrue(PrimitiveLoader.shouldRelocate(missing, false))
        assertTrue(PrimitiveLoader.shouldRelocate(empty, false))
    }
}
