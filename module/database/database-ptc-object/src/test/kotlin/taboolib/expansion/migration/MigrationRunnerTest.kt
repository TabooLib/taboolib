package taboolib.expansion.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import taboolib.expansion.createTestDataSource
import java.net.URLClassLoader
import java.nio.file.Path

class MigrationRunnerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `migrate applies separated statements once`() {
        val migrationDirectory = tempDir.resolve(MigrationFiles.DEFAULT_PATH).toFile()
        migrationDirectory.mkdirs()
        migrationDirectory.resolve("V1__init.sql").writeText(
            """
            CREATE TABLE migration_sample (id TEXT PRIMARY KEY, value INT NOT NULL)
            -- @ptc:statement
            INSERT INTO migration_sample (id, value) VALUES ('a', 1)
            """.trimIndent()
        )

        val dataSource = createTestDataSource()
        URLClassLoader(arrayOf(tempDir.toUri().toURL()), javaClass.classLoader).use { classLoader ->
            val runner = MigrationFiles(classLoader = classLoader).runner(dataSource)
            runner.migrate()
            runner.migrate()
        }

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM migration_sample").use { resultSet ->
                    resultSet.next()
                    assertEquals(1, resultSet.getInt(1))
                }
                statement.executeQuery("SELECT COUNT(*) FROM _ptc_schema_history").use { resultSet ->
                    resultSet.next()
                    assertEquals(1, resultSet.getInt(1))
                }
            }
        }
        dataSource.close()
    }

    @Test
    fun `migrate rejects changed checksum`() {
        val migrationDirectory = tempDir.resolve(MigrationFiles.DEFAULT_PATH).toFile()
        migrationDirectory.mkdirs()
        val script = migrationDirectory.resolve("V1__init.sql")
        script.writeText("CREATE TABLE checksum_sample (id TEXT PRIMARY KEY)")

        val dataSource = createTestDataSource()
        URLClassLoader(arrayOf(tempDir.toUri().toURL()), javaClass.classLoader).use { classLoader ->
            val runner = MigrationFiles(classLoader = classLoader).runner(dataSource)
            runner.migrate()
            script.writeText("CREATE TABLE checksum_sample (id TEXT PRIMARY KEY, value INT)")

            assertThrows(MigrationValidationException::class.java) {
                runner.migrate()
            }
        }
        dataSource.close()
    }
}
