package taboolib.expansion

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import taboolib.module.database.*
import java.util.*
import javax.sql.DataSource

// region 测试用 data class

data class SimpleData(
    @Id val name: String,
    var value: Int,
    var description: String
)

data class PlayerHome(
    @Id val username: UUID,
    @Key @Length(32) val serverName: String,
    var world: String,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float,
    var pitch: Float,
    var active: Boolean
)

data class MultiKeyData(
    @Id val id: String,
    @Key @Length(32) val category: String,
    @Key @Length(32) val subCategory: String,
    var score: Int
)

enum class Color { RED, GREEN, BLUE }

/** 重写了 toString() 的枚举，用于验证 Fix 9 */
enum class Status {
    ACTIVE, INACTIVE;

    override fun toString(): String = name.lowercase()
}

data class TypeVariety(
    @Id val id: String,
    var boolVal: Boolean,
    var byteVal: Byte,
    var shortVal: Short,
    var intVal: Int,
    var longVal: Long,
    var floatVal: Float,
    var doubleVal: Double,
    var stringVal: String,
    var uuidVal: UUID,
    var colorVal: Color
)

/** 同类型多字段，用于验证 Fix 1 */
data class DualStringData(
    @Id val first: String,
    var second: String
)

/** nullable 字段，用于验证 Fix 3/4 */
data class NullableData(
    @Id val id: String,
    var label: String?,
    var note: String?
)

/** 含枚举的持久化测试用 data class，用于验证 Fix 9 */
data class EnumData(
    @Id val id: String,
    var color: Color,
    var status: Status
)

data class AllValData(
    @Id val id: String,
    val fixed: String
)

@NotNull
@UniqueKey
annotation class MarkerForMeta

data class AnnotatedData(
    @Id val pk: String,
    @Key val idx: String,
    @UniqueKey val uniq: String,
    @NotNull val required: String,
    @Length(128) val long128: String,
    @Alias("custom_col") val aliased: String,
    var mutable: String
)

data class WrapperData(
    @Id val id: String,
    var count: Int
) {
    companion object {
        @JvmStatic
        fun wrap(map: BundleMap): WrapperData {
            return WrapperData(map["id"], map["count"])
        }
    }
}

// endregion

// region 工具函数

/**
 * 创建 SQLite 内存 HikariDataSource
 */
fun createTestDataSource(): HikariDataSource {
    val config = HikariConfig()
    config.jdbcUrl = "jdbc:sqlite::memory:"
    config.driverClassName = "org.sqlite.JDBC"
    config.maximumPoolSize = 1
    // SQLite 内存库需要固定连接，否则表在连接关闭后丢失
    config.minimumIdle = 1
    config.idleTimeout = 0
    config.maxLifetime = 0
    return HikariDataSource(config)
}

/**
 * 手动构建 Table + ContainerOperatorImpl，复用 ContainerSQLite 的建表逻辑
 */
fun createTestOperator(type: Class<*>, name: String, dataSource: DataSource): ContainerOperatorImpl {
    val analyzedClass = AnalyzedClass.of(type)
    val table = buildSQLiteTable(analyzedClass, name)
    table.createTable(dataSource)
    return ContainerOperatorImpl(table, dataSource)
}

/**
 * 构建 SQLite Table 对象（复刻 ContainerSQLite.createTableObject 逻辑）
 */
private fun buildSQLiteTable(type: AnalyzedClass, name: String): Table<HostSQLite, SQLite> {
    val fakeHost = HostSQLite(java.io.File("test.db"))
    return Table(name, fakeHost) {
        if (!type.members.any { it.isPrimary }) {
            add { id() }
        }
        type.members.forEach { member ->
            when {
                member.isString || member.isEnum -> add(member.name) {
                    type(ColumnTypeSQLite.TEXT, member.length) { options(member) }
                }
                member.isUUID -> add(member.name) {
                    type(ColumnTypeSQLite.TEXT, 36) { options(member) }
                }
                member.canConvertedInteger() -> add(member.name) {
                    type(ColumnTypeSQLite.INTEGER) { options(member) }
                }
                member.canConvertedDecimal() -> add(member.name) {
                    type(ColumnTypeSQLite.REAL) { options(member) }
                }
                member.isByteArray -> add(member.name) {
                    type(ColumnTypeSQLite.BLOB) { options(member) }
                }
                else -> error("Unsupported type: ${member.name} (${member.returnType})")
            }
        }
    }
}

private fun ColumnSQLite.options(member: AnalyzedClassMember) {
    if (member.isPrimary) {
        options(ColumnOptionSQLite.PRIMARY_KEY)
    }
    if (member.isUniqueKey) {
        options(ColumnOptionSQLite.UNIQUE)
    }
    if (member.isNotNull) {
        options(ColumnOptionSQLite.NOTNULL)
    }
}

/**
 * 创建类似 PersistentContainer 的测试容器，手动管理 DataSource 和表
 */
class TestContainer(val dataSource: HikariDataSource) {

    val operators = mutableMapOf<String, ContainerOperatorImpl>()

    inline fun <reified T> new(name: String = T::class.java.simpleName.let {
        taboolib.expansion.AnalyzedClassMember.Companion.run { it.toColumnName() }
    }): ContainerOperatorImpl {
        return new(T::class.java, name)
    }

    fun <T> new(type: Class<T>, name: String): ContainerOperatorImpl {
        val op = createTestOperator(type, name, dataSource)
        operators[name] = op
        return op
    }

    fun operator(name: String): ContainerOperatorImpl {
        return operators[name] ?: error("Table '$name' not found")
    }

    inline fun <reified T> get(): ContainerOperatorImpl {
        val name = T::class.java.simpleName.let {
            taboolib.expansion.AnalyzedClassMember.Companion.run { it.toColumnName() }
        }
        return operator(name)
    }

    fun <R> transaction(block: TestTransactionContext.() -> R): Result<R> {
        val connection = dataSource.connection
        connection.autoCommit = false
        return try {
            val context = TestTransactionContext(this, connection)
            val result = context.block()
            if (context.shouldRollback()) {
                connection.rollback()
                Result.failure(TransactionRollbackException("Transaction marked for rollback"))
            } else {
                connection.commit()
                Result.success(result)
            }
        } catch (e: Exception) {
            try { connection.rollback() } catch (re: Exception) { e.addSuppressed(re) }
            Result.failure(e)
        } finally {
            try { connection.autoCommit = true } catch (_: Exception) {}
            try { connection.close() } catch (_: Exception) {}
        }
    }

    fun close() {
        dataSource.close()
    }

    fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return JoinQuery(dataSource).also(builder)
    }
}

class TestTransactionContext(
    private val container: TestContainer,
    val connection: java.sql.Connection
) {
    private var rollbackOnly = false
    private val txOperators = mutableMapOf<String, ContainerOperatorImpl>()

    fun operator(name: String): ContainerOperatorImpl {
        return txOperators.getOrPut(name) {
            container.operator(name).withConnection(connection)
        }
    }

    inline fun <reified T> get(): ContainerOperatorImpl {
        val name = T::class.java.simpleName.let {
            taboolib.expansion.AnalyzedClassMember.Companion.run { it.toColumnName() }
        }
        return operator(name)
    }

    fun rollback() {
        rollbackOnly = true
    }

    fun rollbackNow(message: String = "Transaction aborted"): Nothing {
        throw TransactionAbortException(message)
    }

    fun shouldRollback() = rollbackOnly

    fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return JoinQuery(container.dataSource, connection).also(builder)
    }
}

// endregion
