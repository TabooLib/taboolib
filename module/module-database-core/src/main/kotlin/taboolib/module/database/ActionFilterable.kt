package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 支持过滤的行为
 *
 * @author 坏黑
 * @since 2023/9/22 13:17
 */
abstract class ActionFilterable : Filterable(), Action {

    /** 该行为执行完毕后的回调 */
    protected var finallyCallback: (PreparedStatement.(Connection) -> Unit)? = null

    /** 该行为的过滤器 */
    protected var filter: Filter? = null

    /**
     * 追加一个过滤标准：
     * ```
     * where("user" eq user)
     * ```
     * 支持这种写法的前提是这个类必须继承 `Filterable`
     */
    fun where(criteria: Criteria) {
        if (filter == null) {
            filter = Filter()
        }
        filter!!.criteria += criteria
    }

    /**
     * 过滤，函数式写法：
     * ```
     * where { "user" eq user }
     * ```
     */
    fun where(func: Filter.() -> Unit) {
        if (filter == null) {
            filter = Filter().also(func)
        } else {
            func(filter!!)
        }
    }

    override fun append(criteria: Criteria) {
    }

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {
        this.finallyCallback = onFinally
    }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {
        this.finallyCallback?.invoke(preparedStatement, connection)
    }
}