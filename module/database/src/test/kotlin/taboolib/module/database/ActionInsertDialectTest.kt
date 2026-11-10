package taboolib.module.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.sqlite.SQLiteDataSource
import java.io.File

class ActionInsertDialectTest {

    @AfterEach
    fun resetIdentifierQuoter() {
        currentQuoter.remove()
    }

    @Test
    fun `keeps mysql duplicate key syntax`() {
        val host = HostSQL("localhost", "3306", "root", "", "test")
        val action = insertAction(host, "order") {
            onDuplicateKeyUpdate {
                update("value", 2)
            }
        }

        assertEquals(
            "INSERT INTO `order` (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = ?",
            action.query
        )
        assertEquals(listOf("entry", 1, 2), action.elements)
    }

    @Test
    fun `uses postgresql conflict syntax and double quoted identifiers`() {
        val host = HostPostgreSQL("localhost", "5432", "postgres", "", "test")
        val action = insertAction(host, "public.order") {
            onDuplicateKeyUpdate(listOf("key")) {
                update("value", 2)
            }
        }

        assertEquals(
            "INSERT INTO \"public\".\"order\" (\"key\", \"value\") VALUES (?, ?) ON CONFLICT (\"key\") DO UPDATE SET \"value\" = ?",
            action.query
        )
    }

    @Test
    fun `requires explicit postgresql conflict keys`() {
        val host = HostPostgreSQL("localhost", "5432", "postgres", "", "test")
        val action = insertAction(host, "order") {
            onDuplicateKeyUpdate {
                update("value", 2)
            }
        }

        assertThrows(IllegalArgumentException::class.java) { action.query }
    }

    @Test
    fun `uses sqlite conflict syntax without guessing a conflict target`() {
        val action = insertAction(HostSQLite(File("database.db")), "order") {
            onDuplicateKeyUpdate {
                update("value", 2)
            }
        }

        assertEquals(
            "INSERT INTO `order` (`key`, `value`) VALUES (?, ?) ON CONFLICT DO UPDATE SET `value` = ?",
            action.query
        )
    }

    @Test
    fun `keeps positional insert compatibility when keys are omitted`() {
        setupQuoterForHost(HostSQLite(File("database.db")))
        val action = ActionInsert("order", emptyArray()).also { it.value("entry", 1) }

        assertEquals("INSERT INTO `order` VALUES (?, ?)", action.query)
        assertEquals(listOf("entry", 1), action.elements)
    }

    @Test
    fun `rejects incomplete insert statements`() {
        setupQuoterForHost(HostSQLite(File("database.db")))

        val noValues = ActionInsert("order", arrayOf("key"))
        assertThrows(IllegalArgumentException::class.java) { noValues.query }

        val blankKeys = ActionInsert("order", arrayOf(" ")).also { it.value("entry") }
        assertThrows(IllegalArgumentException::class.java) { blankKeys.query }

        val mismatchedValues = ActionInsert("order", arrayOf("key", "value")).also { it.value("entry") }
        assertThrows(IllegalArgumentException::class.java) { mismatchedValues.query }
    }

    @Test
    fun `executes generated sqlite upsert`() {
        val action = insertAction(HostSQLite(File("database.db")), "entries") {
            onDuplicateKeyUpdate {
                update("value", 2)
            }
        }
        val dataSource = SQLiteDataSource().also { it.url = "jdbc:sqlite::memory:" }

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE entries (`key` TEXT PRIMARY KEY, `value` INTEGER)")
            }
            repeat(2) {
                connection.prepareStatement(action.query).use { statement ->
                    action.elements.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
                    statement.executeUpdate()
                }
            }
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT value FROM entries WHERE `key` = 'entry'").use { result ->
                    result.next()
                    assertEquals(2, result.getInt(1))
                }
            }
        }
    }

    @Test
    fun `quotes index names tables and columns for postgresql`() {
        val host = HostPostgreSQL("localhost", "5432", "postgres", "", "test")
        val table = Table<HostPostgreSQL, PostgreSQL>("public.order", host)
        val source = ExecutableSource(table, SQLiteDataSource(), false)
        setupQuoterForHost(host)

        val query = with(source) {
            table.generateCreateIndexQuery(Index("select", listOf("from", "value"), unique = true, checkExists = false))
        }

        assertEquals(
            "CREATE UNIQUE INDEX \"select\" ON \"public\".\"order\" ( \"from\",\"value\" )",
            query
        )
    }

    private fun insertAction(host: Host<*>, table: String, configure: ActionInsert.() -> Unit): ActionInsert {
        setupQuoterForHost(host)
        return ActionInsert(table, arrayOf("key", "value")).also {
            it.setupDialect(host)
            it.value("entry", 1)
            configure(it)
        }
    }
}
