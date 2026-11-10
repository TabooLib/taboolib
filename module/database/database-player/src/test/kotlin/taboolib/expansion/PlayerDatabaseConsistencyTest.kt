package taboolib.expansion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.nio.file.Path
import java.sql.SQLException
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class PlayerDatabaseConsistencyTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `concurrent first writes keep one row`() {
        val fixture = createFixture("concurrent")
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val values = (0 until 32).map { "value-$it" }
        val futures = values.map { value ->
            executor.submit {
                start.await()
                fixture.database["player", "score"] = value
            }
        }

        try {
            start.countDown()
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }

        assertEquals(1, countRows(fixture.dataSource, fixture.table, "player", "score"))
        assertTrue(fixture.database["player", "score"] in values)
    }

    @Test
    fun `legacy duplicate rows keep latest value before unique index creation`() {
        val table = "legacy_player_data"
        val file = tempDir.resolve("legacy.db").toFile()
        val dataSource = createDataSource(file.toPath())
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE `$table` (`user` TEXT, `key` TEXT, `value` TEXT)")
                statement.executeUpdate("INSERT INTO `$table` (`user`, `key`, `value`) VALUES ('player', 'score', 'old')")
                statement.executeUpdate("INSERT INTO `$table` (`user`, `key`, `value`) VALUES ('player', 'score', 'new')")
                statement.executeUpdate("INSERT INTO `$table` (`user`, `key`, `value`) VALUES ('other', 'score', 'kept')")
            }
        }

        val database = Database(TypeSQLite(file, table), dataSource)

        assertEquals("new", database["player", "score"])
        assertEquals(1, countRows(dataSource, table, "player", "score"))
        assertEquals("kept", database["other", "score"])
        assertThrows(SQLException::class.java) {
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO `$table` (`user`, `key`, `value`) VALUES (?, ?, ?)").use { statement ->
                    statement.setString(1, "player")
                    statement.setString(2, "score")
                    statement.setString(3, "duplicate")
                    statement.executeUpdate()
                }
            }
        }
    }

    @Test
    fun `concurrent initialization migrates duplicates once`() {
        val table = "concurrent_migration"
        val file = tempDir.resolve("concurrent-migration.db").toFile()
        val setupDataSource = createDataSource(file.toPath())
        setupDataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE `$table` (`user` TEXT, `key` TEXT, `value` TEXT)")
                statement.executeUpdate("INSERT INTO `$table` (`user`, `key`, `value`) VALUES ('player', 'score', 'old')")
                statement.executeUpdate("INSERT INTO `$table` (`user`, `key`, `value`) VALUES ('player', 'score', 'new')")
            }
        }
        val executor = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        val futures = (0 until 2).map {
            executor.submit<Database> {
                start.await()
                val dataSource = createDataSource(file.toPath())
                Database(TypeSQLite(file, table), dataSource)
            }
        }

        val databases = try {
            start.countDown()
            futures.map { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }

        assertEquals("new", databases.first()["player", "score"])
        assertEquals(1, countRows(setupDataSource, table, "player", "score"))
    }

    @Test
    fun `same named index on wrong columns does not bypass key constraint`() {
        val table = "wrong_index"
        val file = tempDir.resolve("wrong-index.db").toFile()
        val dataSource = createDataSource(file.toPath())
        val expectedIndexName = uniqueIndexName(table)
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE `$table` (`user` TEXT, `key` TEXT, `value` TEXT)")
                statement.executeUpdate("CREATE UNIQUE INDEX `$expectedIndexName` ON `$table` (`value`)")
            }
        }

        val database = Database(TypeSQLite(file, table), dataSource)
        database["player", "score"] = "one"
        database["player", "score"] = "two"

        assertEquals("two", database["player", "score"])
        assertEquals(1, countRows(dataSource, table, "player", "score"))
    }

    @Test
    fun `special table names are quoted for unique index creation`() {
        val fixture = createFixture("special", "player-data")

        fixture.database["player", "score"] = "one"
        fixture.database["player", "score"] = "two"

        assertEquals("two", fixture.database["player", "score"])
        assertEquals(1, countRows(fixture.dataSource, fixture.table, "player", "score"))
    }

    @Test
    fun `queued writes coalesce to latest value`() {
        val fixture = createFixture("coalesced")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        container.asyncExecutor = { tasks.addLast(it) }

        container["score"] = "one"
        container["score"] = "two"

        assertEquals(1, tasks.size)
        tasks.removeFirst().invoke()
        assertEquals("two", fixture.database["player", "score"])
    }

    @Test
    fun `scheduler rejection does not hide a concurrent newer write`() {
        val fixture = createFixture("scheduler-rejection")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        val schedulingStarted = CountDownLatch(1)
        val releaseFirstScheduling = CountDownLatch(1)
        val schedulingAttempts = AtomicInteger()
        container.asyncExecutor = { task ->
            if (schedulingAttempts.getAndIncrement() == 0) {
                schedulingStarted.countDown()
                releaseFirstScheduling.await()
                throw RejectedExecutionException("first scheduling attempt rejected")
            }
            tasks.addLast(task)
        }
        val executor = Executors.newFixedThreadPool(2)
        val secondStarted = CountDownLatch(1)
        val first = executor.submit<Throwable?> {
            runCatching { container["state"] = "old" }.exceptionOrNull()
        }
        assertTrue(schedulingStarted.await(10, TimeUnit.SECONDS))
        val second = executor.submit {
            secondStarted.countDown()
            container["state"] = "new"
        }
        assertTrue(secondStarted.await(10, TimeUnit.SECONDS))
        releaseFirstScheduling.countDown()

        try {
            assertTrue(first.get(10, TimeUnit.SECONDS) is RejectedExecutionException)
            second.get(10, TimeUnit.SECONDS)
        } finally {
            executor.shutdownNow()
        }

        assertEquals(1, tasks.size)
        tasks.removeFirst().invoke()
        assertEquals("new", fixture.database["player", "state"])
    }

    @Test
    fun `delete supersedes queued save without null assertion failure`() {
        val fixture = createFixture("delete")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        container.asyncExecutor = { tasks.addLast(it) }

        container["state"] = "present"
        container["state"] = ""

        assertEquals(1, tasks.size)
        tasks.removeFirst().invoke()
        assertNull(fixture.database["player", "state"])
    }

    @Test
    fun `delayed value is not persisted before its deadline`() {
        val fixture = createFixture("delayed")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        container.asyncExecutor = { tasks.addLast(it) }

        container["state"] = "old"
        container.setDelayed("state", "new", 1, TimeUnit.DAYS)
        tasks.removeFirst().invoke()
        container.checkUpdate()

        assertTrue(tasks.isEmpty())
        assertNull(fixture.database["player", "state"])
        assertEquals("new", container["state"])
    }

    @Test
    fun `concurrent delayed writes retain the latest deadline state`() {
        val fixture = createFixture("concurrent-delayed")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        container.asyncExecutor = { tasks.addLast(it) }
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val values = (1..128).map { "value-$it" }
        val futures = values.mapIndexed { index, value ->
            executor.submit {
                start.await()
                container.setDelayed("state", value, -index.toLong(), TimeUnit.MILLISECONDS)
            }
        }

        try {
            start.countDown()
            futures.forEach { it.get(10, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }

        container.checkUpdate()
        assertEquals(1, tasks.size)
        tasks.removeFirst().invoke()
        assertEquals(container["state"], fixture.database["player", "state"])
    }

    @Test
    fun `elapsed delayed value schedules one write`() {
        val fixture = createFixture("elapsed")
        val container = DataContainer("player", fixture.database)
        val tasks = ArrayDeque<() -> Unit>()
        container.asyncExecutor = { tasks.addLast(it) }

        container.setDelayed("state", "ready", 0, TimeUnit.MILLISECONDS)
        container.checkUpdate()

        assertEquals(1, tasks.size)
        tasks.removeFirst().invoke()
        assertEquals("ready", fixture.database["player", "state"])
    }

    private fun createFixture(name: String, table: String = "${name}_player_data"): Fixture {
        val file = tempDir.resolve("$name.db").toFile()
        val dataSource = createDataSource(file.toPath())
        return Fixture(Database(TypeSQLite(file, table), dataSource), dataSource, table)
    }

    private fun uniqueIndexName(tableName: String): String {
        val normalizedName = tableName.replace(Regex("[^A-Za-z0-9_]"), "_").ifEmpty { "table" }
        return "uk_${normalizedName.take(36)}_${Integer.toHexString(tableName.hashCode())}_user_key"
    }

    private fun createDataSource(path: Path): SQLiteDataSource {
        val config = SQLiteConfig().apply {
            setBusyTimeout(30_000)
            setJournalMode(SQLiteConfig.JournalMode.WAL)
            setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
        }
        return SQLiteDataSource(config).apply {
            url = "jdbc:sqlite:${path.toAbsolutePath()}"
        }
    }

    private fun countRows(dataSource: SQLiteDataSource, table: String, user: String, key: String): Int {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM `$table` WHERE `user` = ? AND `key` = ?").use { statement ->
                statement.setString(1, user)
                statement.setString(2, key)
                statement.executeQuery().use { result ->
                    result.next()
                    result.getInt(1)
                }
            }
        }
    }

    private data class Fixture(
        val database: Database,
        val dataSource: SQLiteDataSource,
        val table: String,
    )
}
