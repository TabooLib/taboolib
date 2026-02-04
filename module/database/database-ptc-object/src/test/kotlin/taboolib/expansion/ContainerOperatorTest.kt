package taboolib.expansion

import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContainerOperatorTest {

    private lateinit var ds: HikariDataSource
    private lateinit var operator: ContainerOperatorImpl

    @BeforeEach
    fun setUp() {
        AnalyzedClass.cached.clear()
        ds = createTestDataSource()
        operator = createTestOperator(SimpleData::class.java, "simple_data", ds)
    }

    @AfterEach
    fun tearDown() {
        ds.close()
    }

    // region insert + find/findOne

    @Test
    fun `insert and findOne by primary key`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        val found = operator.findOne(SimpleData::class.java, "a")
        assertNotNull(found)
        assertEquals("a", found!!.name)
        assertEquals(1, found.value)
        assertEquals("alpha", found.description)
    }

    @Test
    fun `insert multiple and find by primary key`() {
        operator.insert(listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        // "a" 只有一条
        val listA = operator.find(SimpleData::class.java, "a")
        assertEquals(1, listA.size)
        // 总共两条
        val all = operator.get(SimpleData::class.java)
        assertEquals(2, all.size)
    }

    @Test
    fun `findOne returns null when not found`() {
        val result = operator.findOne(SimpleData::class.java, "nonexistent")
        assertNull(result)
    }

    // endregion

    // region get/getOne

    @Test
    fun `get returns all rows`() {
        operator.insert(listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta"),
            SimpleData("c", 3, "gamma")
        ))
        val all = operator.get(SimpleData::class.java)
        assertEquals(3, all.size)
    }

    @Test
    fun `getOne returns first row`() {
        operator.insert(listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        val one = operator.getOne(SimpleData::class.java)
        assertNotNull(one)
    }

    @Test
    fun `getOne returns null on empty table`() {
        val one = operator.getOne(SimpleData::class.java)
        assertNull(one)
    }

    // endregion

    // region update

    @Test
    fun `update existing data`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        val data = SimpleData("a", 99, "updated")
        operator.update(data)
        val found = operator.findOne(SimpleData::class.java, "a")!!
        assertEquals(99, found.value)
        assertEquals("updated", found.description)
    }

    @Test
    fun `update inserts when not exists`() {
        val data = SimpleData("new", 42, "fresh")
        operator.update(data)
        val found = operator.findOne(SimpleData::class.java, "new")
        assertNotNull(found)
        assertEquals(42, found!!.value)
    }

    // endregion

    // region updateByKey

    @Test
    fun `updateByKey with @Key`() {
        val mkDs = createTestDataSource()
        try {
            val mkOp = createTestOperator(MultiKeyData::class.java, "multi_key_data", mkDs)
            mkOp.insert(listOf(MultiKeyData("id1", "cat1", "sub1", 10)))
            // 用 @Id + @Key 定位更新
            val updated = MultiKeyData("id1", "cat1", "sub1", 99)
            mkOp.updateByKey(updated)
            val found = mkOp.findOne(MultiKeyData::class.java, "id1") {
                "category" eq "cat1"
                "sub_category" eq "sub1"
            }!!
            assertEquals(99, found.score)
        } finally {
            mkDs.close()
        }
    }

    // endregion

    // region has

    @Test
    fun `has returns true when data exists`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        assertTrue(operator.has(SimpleData::class.java, "a"))
    }

    @Test
    fun `has returns false when data not exists`() {
        assertFalse(operator.has(SimpleData::class.java, "missing"))
    }

    @Test
    fun `has with filter`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        assertTrue(operator.has { "name" eq "a" })
        assertFalse(operator.has { "name" eq "zzz" })
    }

    // endregion

    // region delete

    @Test
    fun `delete removes data`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        assertTrue(operator.has(SimpleData::class.java, "a"))
        operator.delete(SimpleData::class.java, "a")
        assertFalse(operator.has(SimpleData::class.java, "a"))
    }

    // endregion

    // region sort

    @Test
    fun `sort ascending`() {
        operator.insert(listOf(
            SimpleData("c", 3, "gamma"),
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        val sorted = operator.sort(SimpleData::class.java, "value", 10)
        assertEquals(listOf(1, 2, 3), sorted.map { it.value })
    }

    @Test
    fun `sortDescending`() {
        operator.insert(listOf(
            SimpleData("c", 3, "gamma"),
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        val sorted = operator.sortDescending(SimpleData::class.java, "value", 10)
        assertEquals(listOf(3, 2, 1), sorted.map { it.value })
    }

    @Test
    fun `sort with limit`() {
        operator.insert(listOf(
            SimpleData("c", 3, "gamma"),
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        val sorted = operator.sort(SimpleData::class.java, "value", 2)
        assertEquals(2, sorted.size)
        assertEquals(listOf(1, 2), sorted.map { it.value })
    }

    // endregion

    // region filter

    @Test
    fun `get with filter`() {
        operator.insert(listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta"),
            SimpleData("c", 3, "gamma")
        ))
        val filtered = operator.get(SimpleData::class.java) { "value" gt 1 }
        assertEquals(2, filtered.size)
    }

    // endregion

    // region Fix 9: Enum 序列化用 name 而非 toString

    @Test
    fun `enum with overridden toString serializes and deserializes correctly`() {
        val enumDs = createTestDataSource()
        try {
            val enumOp = createTestOperator(EnumData::class.java, "enum_data", enumDs)
            val data = EnumData("e1", Color.RED, Status.ACTIVE)
            enumOp.insert(listOf(data))
            val found = enumOp.findOne(EnumData::class.java, "e1")!!
            assertEquals(Color.RED, found.color)
            assertEquals(Status.ACTIVE, found.status)
        } finally {
            enumDs.close()
        }
    }

    @Test
    fun `enum update preserves correct values`() {
        val enumDs = createTestDataSource()
        try {
            val enumOp = createTestOperator(EnumData::class.java, "enum_data2", enumDs)
            enumOp.insert(listOf(EnumData("e1", Color.RED, Status.ACTIVE)))
            val updated = EnumData("e1", Color.BLUE, Status.INACTIVE)
            enumOp.update(updated)
            val found = enumOp.findOne(EnumData::class.java, "e1")!!
            assertEquals(Color.BLUE, found.color)
            assertEquals(Status.INACTIVE, found.status)
        } finally {
            enumDs.close()
        }
    }

    // endregion

    // region Fix 6: update 事务保护

    @Test
    fun `update existing data uses transaction`() {
        // 验证 update 的 check-then-act 在事务中执行不会异常
        operator.insert(listOf(SimpleData("tx1", 1, "original")))
        operator.update(SimpleData("tx1", 99, "updated"))
        val found = operator.findOne(SimpleData::class.java, "tx1")!!
        assertEquals(99, found.value)
        assertEquals("updated", found.description)
    }

    @Test
    fun `update non-existing data inserts in transaction`() {
        operator.update(SimpleData("tx2", 42, "fresh"))
        val found = operator.findOne(SimpleData::class.java, "tx2")
        assertNotNull(found)
        assertEquals(42, found!!.value)
    }

    // endregion
}
