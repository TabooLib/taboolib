package taboolib.expansion

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * JoinQuery 多表联查测试
 *
 * @author sky
 */
class JoinQueryTest {

    private lateinit var container: TestContainer

    // region 测试用 data class

    data class Player(
        @Id val username: String,
        var displayName: String,
        var active: Boolean
    )

    data class PlayerStats(
        @Id val username: String,
        var level: Int,
        var score: Int
    )

    data class PlayerHome(
        @Id val id: Int,
        val username: String,
        @Length(32) val homeName: String,
        var world: String,
        var x: Double
    )

    data class PlayerSummary(
        val username: String,
        @Alias("display_name") val displayName: String,
        val level: Int
    )

    // endregion

    @BeforeEach
    fun setUp() {
        AnalyzedClass.cached.clear()
        val ds = createTestDataSource()
        container = TestContainer(ds)
        container.new<Player>("player")
        container.new<PlayerStats>("player_stats")
        container.new<PlayerHome>("player_home")
        // 插入测试数据
        val playerOp = container.operator("player")
        playerOp.insert(listOf(
            Player("alice", "Alice A", true),
            Player("bob", "Bob B", true),
            Player("charlie", "Charlie C", false)
        ))
        val statsOp = container.operator("player_stats")
        statsOp.insert(listOf(
            PlayerStats("alice", 50, 1000),
            PlayerStats("bob", 30, 500)
        ))
        val homeOp = container.operator("player_home")
        homeOp.insert(listOf(
            PlayerHome(1, "alice", "main", "world", 100.0),
            PlayerHome(2, "alice", "nether", "world_nether", 200.0),
            PlayerHome(3, "bob", "main", "world", 300.0)
        ))
    }

    @AfterEach
    fun tearDown() {
        container.close()
    }

    @Test
    fun `inner join basic query`() {
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
        }.execute()
        // alice 和 bob 在两表都有记录，charlie 没有 stats
        assertEquals(2, results.size)
        val names = results.map { it.get<String>("username") }.toSet()
        assertTrue(names.contains("alice"))
        assertTrue(names.contains("bob"))
        assertFalse(names.contains("charlie"))
    }

    @Test
    fun `left join with null values`() {
        val results = container.join {
            from("player")
            leftJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            select("player.username", "player.display_name", "player_stats.level")
        }.execute()
        // 所有 player 都应返回（LEFT JOIN）
        assertEquals(3, results.size)
        // charlie 没有 stats，level 应为 null
        val charlie = results.first { it.get<String>("username") == "charlie" }
        assertNull(charlie.getOrNull<Int>("level"))
    }

    @Test
    fun `three table join`() {
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            leftJoin("player_home") {
                on("player.username" eq pre("player_home.username"))
            }
            select("player.username", "player_stats.level", "player_home.world")
        }.execute()
        // alice: 2 homes -> 2 rows; bob: 1 home -> 1 row
        assertEquals(3, results.size)
    }

    @Test
    fun `mapTo auto mapping to data class`() {
        // PlayerSummary 需要单独注册，因为 AnalyzedClass 会分析它
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            select("player.username", "player.display_name", "player_stats.level")
        }.mapTo<PlayerSummary>()
        assertEquals(2, results.size)
        val alice = results.first { it.username == "alice" }
        assertEquals("Alice A", alice.displayName)
        assertEquals(50, alice.level)
    }

    @Test
    fun `map with custom mapper`() {
        val names = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
        }.map { row ->
            "${row.get<String>("display_name")} (Lv.${row.get<Any>("level")})"
        }
        assertEquals(2, names.size)
        assertTrue(names.any { it.contains("Alice A") })
    }

    @Test
    fun `executeOne returns single result`() {
        val result = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            where { "player.username" eq "alice" }
        }.executeOne()
        assertNotNull(result)
        assertEquals("alice", result!!.get<String>("username"))
    }

    @Test
    fun `executeOne returns null when no match`() {
        val result = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            where { "player.username" eq "nonexistent" }
        }.executeOne()
        assertNull(result)
    }

    @Test
    fun `where filter`() {
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            where { "player_stats.level" gt 40 }
        }.execute()
        assertEquals(1, results.size)
        assertEquals("alice", results[0].get<String>("username"))
    }

    @Test
    fun `orderBy and limit`() {
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            orderBy("player_stats.level", taboolib.module.database.Order.Type.DESC)
            limit(1)
        }.execute()
        assertEquals(1, results.size)
        assertEquals("alice", results[0].get<String>("username"))
    }

    @Test
    fun `offset with limit`() {
        val results = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            orderBy("player_stats.level", taboolib.module.database.Order.Type.DESC)
            limit(1)
            offset(1)
        }.execute()
        assertEquals(1, results.size)
        assertEquals("bob", results[0].get<String>("username"))
    }

    @Test
    fun `transaction join`() {
        val result = container.transaction {
            val op = operator("player")
            op.insert(listOf(Player("dave", "Dave D", true)))
            val statsOp = operator("player_stats")
            statsOp.insert(listOf(PlayerStats("dave", 99, 9999)))
            join {
                from("player")
                innerJoin("player_stats") {
                    on("player.username" eq pre("player_stats.username"))
                }
                where { "player.username" eq "dave" }
            }.execute()
        }
        assertTrue(result.isSuccess)
        val rows = result.getOrNull()!!
        assertEquals(1, rows.size)
        assertEquals("dave", rows[0].get<String>("username"))
    }

    @Test
    fun `error when from not called`() {
        assertThrows(IllegalStateException::class.java) {
            container.join {
                innerJoin("player_stats") {
                    on("player.username" eq pre("player_stats.username"))
                }
            }.execute()
        }
    }

    @Test
    fun `mapOneTo returns single data class`() {
        val summary = container.join {
            from("player")
            innerJoin("player_stats") {
                on("player.username" eq pre("player_stats.username"))
            }
            select("player.username", "player.display_name", "player_stats.level")
            where { "player.username" eq "bob" }
        }.mapOneTo<PlayerSummary>()
        assertNotNull(summary)
        assertEquals("bob", summary!!.username)
        assertEquals("Bob B", summary.displayName)
        assertEquals(30, summary.level)
    }
}
