package taboolib.expansion.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import taboolib.expansion.Id
import taboolib.expansion.persistentContainer
import taboolib.module.configuration.Configuration
import taboolib.module.database.Database
import java.lang.reflect.Proxy
import java.net.URLClassLoader
import java.nio.file.Path
import java.sql.DriverManager

class PersistentContainerMigrationTest {

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        Database.settingsFile = Proxy.newProxyInstance(
            Configuration::class.java.classLoader,
            arrayOf(Configuration::class.java),
        ) { _, method, args ->
            when (method.name) {
                "contains" -> false
                "getBoolean", "getInt", "getLong", "getString" -> args?.getOrNull(1)
                "getConfigurationSection", "getFile" -> null
                "getReloadGeneration" -> 0
                "saveToString" -> ""
                else -> null
            }
        } as Configuration
    }

    @Test
    fun `persistent container migrates existing database before binding operators`() {
        val databaseFile = tempDir.resolve("existing.db").toFile()
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE e2e_existing_data (id TEXT PRIMARY KEY)")
                statement.executeUpdate("INSERT INTO e2e_existing_data (id) VALUES ('old')")
            }
        }
        val migrationDirectory = tempDir.resolve(MigrationFiles.DEFAULT_PATH).toFile()
        migrationDirectory.mkdirs()
        migrationDirectory.resolve("V1__add_value.sql").writeText(
            """
            ALTER TABLE e2e_existing_data ADD COLUMN value INT DEFAULT 0
            -- @ptc:statement
            UPDATE e2e_existing_data SET value = 7 WHERE id = 'old'
            """.trimIndent()
        )

        URLClassLoader(arrayOf(tempDir.toUri().toURL()), javaClass.classLoader).use { classLoader ->
            val container = persistentContainer(type = databaseFile) {
                migrations(classLoader = classLoader)
                new<ExistingMigrationData>("e2e_existing_data")
            }
            container.close()
        }

        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT value FROM e2e_existing_data WHERE id = 'old'").use { resultSet ->
                    assertTrue(resultSet.next())
                    assertEquals(7, resultSet.getInt("value"))
                }
                statement.executeQuery("SELECT COUNT(*) FROM _ptc_schema_history").use { resultSet ->
                    resultSet.next()
                    assertEquals(1, resultSet.getInt(1))
                }
            }
        }
    }

    @Test
    fun `persistent container baselines fresh database without running historical scripts`() {
        val databaseFile = tempDir.resolve("fresh.db").toFile()
        val migrationDirectory = tempDir.resolve(MigrationFiles.DEFAULT_PATH).toFile()
        migrationDirectory.mkdirs()
        migrationDirectory.resolve("V1__historical_change.sql").writeText(
            "ALTER TABLE e2e_fresh_data ADD COLUMN historical_value INT"
        )

        URLClassLoader(arrayOf(tempDir.toUri().toURL()), javaClass.classLoader).use { classLoader ->
            val firstContainer = persistentContainer(type = databaseFile) {
                migrations(classLoader = classLoader)
                new<FreshMigrationData>("e2e_fresh_data")
            }
            firstContainer.close()

            val secondContainer = persistentContainer(type = databaseFile) {
                migrations(classLoader = classLoader)
                new<FreshMigrationData>("e2e_fresh_data")
            }
            secondContainer.close()
        }

        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info(e2e_fresh_data)").use { resultSet ->
                    val columns = mutableSetOf<String>()
                    while (resultSet.next()) {
                        columns += resultSet.getString("name")
                    }
                    assertTrue("id" in columns)
                    assertTrue("value" in columns)
                    assertTrue("historical_value" !in columns)
                }
                statement.executeQuery("SELECT COUNT(*) FROM _ptc_schema_history").use { resultSet ->
                    resultSet.next()
                    assertEquals(1, resultSet.getInt(1))
                }
            }
        }
    }

}

data class ExistingMigrationData(
    @Id val id: String,
    val value: Int,
)

data class FreshMigrationData(
    @Id val id: String,
    val value: Int,
)
