package taboolib.expansion

import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionTest {

    private lateinit var container: TestContainer

    @BeforeEach
    fun setUp() {
        AnalyzedClass.cached.clear()
        val ds = createTestDataSource()
        container = TestContainer(ds)
        container.new<SimpleData>("simple_data")
    }

    @AfterEach
    fun tearDown() {
        container.close()
    }

    @Test
    fun `transaction commits on success`() {
        val result = container.transaction {
            val op = operator("simple_data")
            op.insert(listOf(SimpleData("a", 1, "alpha")))
            "done"
        }
        assertTrue(result.isSuccess)
        assertEquals("done", result.getOrNull())
        val all = container.operator("simple_data").get(SimpleData::class.java)
        assertEquals(1, all.size)
    }

    @Test
    fun `transaction rollback() marks for rollback`() {
        container.operator("simple_data").insert(listOf(SimpleData("pre", 0, "before")))
        val result = container.transaction {
            val op = operator("simple_data")
            op.insert(listOf(SimpleData("a", 1, "alpha")))
            rollback()
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TransactionRollbackException)
        // 事务前的数据仍在，事务中插入的数据被回滚
        val all = container.operator("simple_data").get(SimpleData::class.java)
        assertEquals(1, all.size)
        assertEquals("pre", all[0].name)
    }

    @Test
    fun `transaction rollbackNow() immediately aborts`() {
        container.operator("simple_data").insert(listOf(SimpleData("pre", 0, "before")))
        val result = container.transaction {
            val op = operator("simple_data")
            op.insert(listOf(SimpleData("a", 1, "alpha")))
            rollbackNow("abort!")
            @Suppress("UNREACHABLE_CODE")
            op.insert(listOf(SimpleData("b", 2, "beta"))) // 不会执行
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TransactionAbortException)
        val all = container.operator("simple_data").get(SimpleData::class.java)
        assertEquals(1, all.size)
        assertEquals("pre", all[0].name)
    }

    @Test
    fun `transaction rolls back on exception`() {
        container.operator("simple_data").insert(listOf(SimpleData("pre", 0, "before")))
        val result = container.transaction {
            val op = operator("simple_data")
            op.insert(listOf(SimpleData("a", 1, "alpha")))
            error("boom")
        }
        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()!!.message)
        val all = container.operator("simple_data").get(SimpleData::class.java)
        assertEquals(1, all.size)
    }

    @Test
    fun `cross-table transaction`() {
        // 注册第二张表
        container.new<AllValData>("all_val_data")
        val result = container.transaction {
            val op1 = operator("simple_data")
            val op2 = operator("all_val_data")
            op1.insert(listOf(SimpleData("a", 1, "alpha")))
            op2.insert(listOf(AllValData("x", "fixed")))
            "ok"
        }
        assertTrue(result.isSuccess)
        assertEquals(1, container.operator("simple_data").get(SimpleData::class.java).size)
        assertEquals(1, container.operator("all_val_data").get(AllValData::class.java).size)
    }

    @Test
    fun `cross-table transaction rollback affects all tables`() {
        container.new<AllValData>("all_val_data")
        val result = container.transaction {
            val op1 = operator("simple_data")
            val op2 = operator("all_val_data")
            op1.insert(listOf(SimpleData("a", 1, "alpha")))
            op2.insert(listOf(AllValData("x", "fixed")))
            rollback()
        }
        assertTrue(result.isFailure)
        assertEquals(0, container.operator("simple_data").get(SimpleData::class.java).size)
        assertEquals(0, container.operator("all_val_data").get(AllValData::class.java).size)
    }
}
