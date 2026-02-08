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

    /** 语句 */
    override val query: String
        get() = Statement("INSERT INTO")
            .addSegment(table.asFormattedColumnName())
            .addSegmentIfTrue(keys.isNotEmpty()) {
                addKeys(keys)
            }
            .addSegmentIfTrue(values.isNotEmpty()) {
                addSegment("VALUES")
                addValues(values)
            }
            .addSegmentIfTrue(duplicateUpdate.isNotEmpty()) {
                addSegment("ON DUPLICATE KEY UPDATE")
                addOperations(duplicateUpdate)
            }.build()

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

    /** 重复时更新 */
    fun onDuplicateKeyUpdate(func: DuplicateUpdateBehavior.() -> Unit) {
        duplicateUpdate = DuplicateUpdateBehavior().also(func).updateOperations
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.finallyCallback = onFinally
    }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.finallyCallback?.invoke(preparedStatement, connection)
    }

    class DuplicateUpdateBehavior {

        val updateOperations = ArrayList<UpdateOperation>()

        fun update(key: String, value: Any) {
            updateOperations += if (value is PreValue) {
                UpdateOperation("${key.asFormattedColumnName()} = ${value.asFormattedColumnName()}")
            } else {
                UpdateOperation("${key.asFormattedColumnName()} = ?", value)
            }
        }
    }
}