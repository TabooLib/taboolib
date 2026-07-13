package taboolib.expansion

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import taboolib.module.configuration.Configuration
import java.lang.reflect.Proxy
import java.nio.file.Path
import javax.sql.DataSource

class DatabaseLifecycleTest {

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setupDatabaseSettings() {
        taboolib.module.database.Database.settingsFile = Proxy.newProxyInstance(
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
    fun `default data source is owned and closed idempotently`() {
        val database = Database(TypeSQLite(tempDir.resolve("owned.db").toFile(), "owned_data"))
        val dataSource = database.dataSource

        assertTrue(database.ownsDataSource)
        database.close()
        database.close()

        assertTrue(dataSource.isClosed())
    }

    @Test
    fun `injected data source remains caller owned by default`() {
        val type = TypeSQLite(tempDir.resolve("borrowed.db").toFile(), "borrowed_data")
        val dataSource = type.host().createDataSource(autoRelease = false)
        val database = Database(type, dataSource)

        assertFalse(database.ownsDataSource)
        database.close()

        assertFalse(dataSource.isClosed())
        (dataSource as AutoCloseable).close()
    }

    @Test
    fun `injected data source can transfer ownership explicitly`() {
        val type = TypeSQLite(tempDir.resolve("transferred.db").toFile(), "transferred_data")
        val dataSource = type.host().createDataSource(autoRelease = false)
        val database = Database(type, dataSource, ownsDataSource = true)

        assertTrue(database.ownsDataSource)
        database.close()

        assertTrue(dataSource.isClosed())
    }

    private fun DataSource.isClosed(): Boolean {
        return javaClass.getMethod("isClosed").invoke(this) as Boolean
    }
}
