package taboolib.expansion

import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpsertTest {

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

    @Test
    fun `upsert inserts new data`() {
        val dataList = listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta"),
            SimpleData("c", 3, "gamma")
        )
        operator.upsert(dataList)
        val all = operator.get(SimpleData::class.java)
        assertEquals(3, all.size)
    }

    @Test
    fun `upsert updates existing data`() {
        operator.insert(listOf(
            SimpleData("a", 1, "alpha"),
            SimpleData("b", 2, "beta")
        ))
        val updated = listOf(
            SimpleData("a", 10, "alpha_updated"),
            SimpleData("b", 20, "beta_updated")
        )
        operator.upsert(updated)
        val all = operator.get(SimpleData::class.java)
        assertEquals(2, all.size)
        val a = operator.findOne(SimpleData::class.java, "a")!!
        assertEquals(10, a.value)
        assertEquals("alpha_updated", a.description)
    }

    @Test
    fun `upsert mixed insert and update`() {
        operator.insert(listOf(SimpleData("a", 1, "alpha")))
        val mixed = listOf(
            SimpleData("a", 99, "updated"),
            SimpleData("b", 2, "new")
        )
        operator.upsert(mixed)
        val all = operator.get(SimpleData::class.java)
        assertEquals(2, all.size)
        assertEquals(99, operator.findOne(SimpleData::class.java, "a")!!.value)
        assertEquals(2, operator.findOne(SimpleData::class.java, "b")!!.value)
    }

    @Test
    fun `upsert with @Key fields`() {
        val mkDs = createTestDataSource()
        try {
            val mkOp = createTestOperator(MultiKeyData::class.java, "multi_key_data", mkDs)
            mkOp.insert(listOf(
                MultiKeyData("id1", "cat1", "sub1", 10),
                MultiKeyData("id2", "cat1", "sub2", 20)
            ))
            // id1+cat1+sub1 已存在，应更新; id3+cat2+sub1 不存在，应插入
            val batch = listOf(
                MultiKeyData("id1", "cat1", "sub1", 99),
                MultiKeyData("id3", "cat2", "sub1", 50)
            )
            mkOp.upsert(batch)
            val all = mkOp.get(MultiKeyData::class.java)
            assertEquals(3, all.size)
            val id1 = mkOp.findOne(MultiKeyData::class.java, "id1")!!
            assertEquals(99, id1.score)
            val id3 = mkOp.findOne(MultiKeyData::class.java, "id3")!!
            assertEquals(50, id3.score)
        } finally {
            mkDs.close()
        }
    }

    @Test
    fun `upsert empty list does nothing`() {
        operator.upsert(emptyList())
        val all = operator.get(SimpleData::class.java)
        assertTrue(all.isEmpty())
    }
}
