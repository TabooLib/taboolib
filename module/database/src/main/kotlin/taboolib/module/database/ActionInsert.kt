package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 一个插入行为
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionInsert(val table: String, val keys: Array<String>) : Action {

    /** 该行为执行完毕后的回调 */
    private var finallyCallback: (PreparedStatement.(Connection) -> Unit)? = null

    /** 插入值 */
    private var values = ArrayList<Array<Any?>>()

    /** 重复时更新 */
    private var duplicateUpdate = ArrayList<UpdateOperation>()

    /** 重复键方言 */
    private var duplicateKeyDialect = DuplicateKeyDialect.MYSQL

    /** 冲突目标 */
    private var conflictKeys: Array<String>? = null

    /** 语句 */
    override val query: String
        get() {
            require(table.isNotBlank()) { "Insert table must not be blank" }
            require(keys.none { it.isBlank() }) { "Insert keys must not contain blank names" }
            require(values.isNotEmpty()) { "Insert values must not be empty" }
            if (keys.isNotEmpty()) {
                require(values.all { it.size == keys.size }) { "Insert value count must match key count" }
            }
            return Statement("INSERT INTO")
                .addSegment(table.asFormattedColumnName())
                .addSegmentIfTrue(keys.isNotEmpty()) {
                    addKeys(keys)
                }
                .addSegment("VALUES")
                .addValues(values)
                .addSegmentIfTrue(duplicateUpdate.isNotEmpty()) {
                    addDuplicateUpdate()
                }.build()
        }

    /** 元素 */
    override val elements: List<Any?>
        get() {
            val el = ArrayList<Any?>()
            el.addAll(values.flatMap { it.toList() })
            el.addAll(duplicateUpdate.mapNotNull { it.value })
            return el
        }

    /** 插入值 */
    fun value(vararg args: Any?) {
        values.add(arrayOf(*args))
    }

    /** 插入值 */
    fun values(args: Array<Any?>) {
        values.add(args)
    }

    /** 插入值 */
    fun values(args: List<Any?>) {
        values.add(args.toTypedArray())
    }

    /**
     * 重复时更新。
     * PostgreSQL 无法从插入字段可靠推断唯一约束，需使用带冲突字段的重载。
     */
    fun onDuplicateKeyUpdate(func: DuplicateUpdateBehavior.() -> Unit) {
        setupDuplicateUpdate(null, func)
    }

    /**
     * 重复时更新，并显式指定 PostgreSQL/SQLite 的冲突字段。
     * MySQL 会忽略冲突字段并继续使用 ON DUPLICATE KEY UPDATE。
     */
    fun onDuplicateKeyUpdate(conflictKeys: Collection<String>, func: DuplicateUpdateBehavior.() -> Unit) {
        setupDuplicateUpdate(conflictKeys.toTypedArray(), func)
    }

    internal fun setupDialect(host: Host<*>) {
        duplicateKeyDialect = when (host) {
            is HostPostgreSQL -> DuplicateKeyDialect.POSTGRESQL
            is HostSQLite -> DuplicateKeyDialect.SQLITE
            else -> DuplicateKeyDialect.MYSQL
        }
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.finallyCallback = onFinally
    }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.finallyCallback?.invoke(preparedStatement, connection)
    }

    private fun setupDuplicateUpdate(conflictKeys: Array<String>?, func: DuplicateUpdateBehavior.() -> Unit) {
        val behavior = DuplicateUpdateBehavior().also(func)
        duplicateUpdate = behavior.updateOperations
        this.conflictKeys = conflictKeys
    }

    private fun Statement.addDuplicateUpdate() {
        when (duplicateKeyDialect) {
            DuplicateKeyDialect.MYSQL -> {
                addSegment("ON DUPLICATE KEY UPDATE")
                addOperations(duplicateUpdate)
            }
            DuplicateKeyDialect.POSTGRESQL -> {
                val targetKeys = conflictKeys
                require(!targetKeys.isNullOrEmpty()) {
                    "PostgreSQL duplicate update requires explicit conflict keys"
                }
                require(targetKeys.none { it.isBlank() }) {
                    "PostgreSQL conflict keys must not contain blank names"
                }
                addSegment("ON CONFLICT")
                addKeys(targetKeys)
                addSegment("DO UPDATE SET")
                addOperations(duplicateUpdate)
            }
            DuplicateKeyDialect.SQLITE -> {
                addSegment("ON CONFLICT")
                conflictKeys?.also { targetKeys ->
                    require(targetKeys.none { it.isBlank() }) {
                        "SQLite conflict keys must not contain blank names"
                    }
                    if (targetKeys.isNotEmpty()) {
                        addKeys(targetKeys)
                    }
                }
                addSegment("DO UPDATE SET")
                addOperations(duplicateUpdate)
            }
        }
    }

    class DuplicateUpdateBehavior {

        val updateOperations = ArrayList<UpdateOperation>()

        fun update(key: String, value: Any) {
            require(key.isNotBlank()) { "Duplicate update key must not be blank" }
            updateOperations += if (value is PreValue) {
                UpdateOperation("${key.asFormattedColumnName()} = ${value.asFormattedColumnName()}")
            } else {
                UpdateOperation("${key.asFormattedColumnName()} = ?", value)
            }
        }
    }

    private enum class DuplicateKeyDialect {
        MYSQL,
        POSTGRESQL,
        SQLITE,
    }
}
