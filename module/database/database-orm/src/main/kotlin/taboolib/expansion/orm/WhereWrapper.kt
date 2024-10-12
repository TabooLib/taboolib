package taboolib.expansion.orm

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.DeleteBuilder
import com.j256.ormlite.stmt.QueryBuilder
import com.j256.ormlite.stmt.UpdateBuilder
import com.j256.ormlite.stmt.Where
import java.util.*
import kotlin.reflect.KProperty

/**
 *  创造 Update 语句构造器
 */
inline fun <reified T, ID> Dao<T, ID>.update(wrapper: UpdateBuilder<T, ID>.() -> Unit): UpdateBuilder<T, ID> {
    val updateBuilder = this.updateBuilder()
    wrapper.invoke(updateBuilder)
    return updateBuilder
}

/**
 *  简易的 Update 操作
 */
inline fun <reified T, ID> Dao<T, ID>.fastUpdate(wrapper: WhereWrapper<T, ID>.() -> Unit): Int {
    val queryBuilder = this.updateBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return queryBuilder.update()
}

/**
 *  在 Update 语句构造器中快速构造 Where 语句
 */
inline fun <reified T, ID> UpdateBuilder<T, ID>.whereWrapper(wrapper: WhereWrapper<T, ID>.() -> Unit): UpdateBuilder<T, ID> {
    val where = this.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return this
}

/**
 *  创造 Delete 语句构造器
 */
inline fun <reified T, ID> Dao<T, ID>.delete(wrapper: DeleteBuilder<T, ID>.() -> Unit): DeleteBuilder<T, ID> {
    val deleteBuilder = this.deleteBuilder()
    wrapper.invoke(deleteBuilder)
    return deleteBuilder
}

/**
 *  简易的 Delete 操作
 */
inline fun <reified T, ID> Dao<T, ID>.fastDelete(wrapper: WhereWrapper<T, ID>.() -> Unit): Int {
    val queryBuilder = this.deleteBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return queryBuilder.delete()
}

/**
 *  在 Delete 语句构造器中快速构造 Where 语句
 */
inline fun <reified T, ID> DeleteBuilder<T, ID>.whereWrapper(wrapper: WhereWrapper<T, ID>.() -> Unit): DeleteBuilder<T, ID> {
    val where = this.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return this
}

/**
 *  创造 Query/Select 语句构造器
 */
inline fun <reified T, ID> Dao<T, ID>.query(wrapper: QueryBuilder<T, ID>.() -> Unit): QueryBuilder<T, ID> {
    val queryBuilder = this.queryBuilder()
    wrapper.invoke(queryBuilder)
    return queryBuilder
}

inline fun <reified T, ID> Dao<T, ID>.select(wrapper: QueryBuilder<T, ID>.() -> Unit): QueryBuilder<T, ID> {
    val queryBuilder = this.queryBuilder()
    wrapper.invoke(queryBuilder)
    return queryBuilder
}

/**
 *  简易的 Select 操作
 */
inline fun <reified T, ID> Dao<T, ID>.fastSelect(wrapper: WhereWrapper<T, ID>.() -> Unit): T {
    val queryBuilder = this.queryBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return where.queryForFirst()
}

/**
 *  在 Select 语句构造器中快速构造 Where 语句
 */
inline fun <reified T, ID> QueryBuilder<T, ID>.whereWrapper(wrapper: WhereWrapper<T, ID>.() -> Unit): QueryBuilder<T, ID> {
    val where = this.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return this
}

/**
 *  简易的 Select 操作 - 返回列表
 */
inline fun <reified T, ID> Dao<T, ID>.fastSelectList(wrapper: WhereWrapper<T, ID>.() -> Unit): MutableList<T> {
    val queryBuilder = this.queryBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return where.query() ?: mutableListOf()
}

/**
 *  简易的 Select 操作 - 返回数量
 */
inline fun <reified T, ID> Dao<T, ID>.countOf(wrapper: WhereWrapper<T, ID>.() -> Unit): Long {
    val queryBuilder = this.queryBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return where.countOf()
}

/**
 *  简易的 Select 操作 - 返回迭代器
 */
inline fun <reified T, ID> Dao<T, ID>.iterator(wrapper: WhereWrapper<T, ID>.() -> Unit): Iterator<T> {
    val queryBuilder = this.queryBuilder()
    val where = queryBuilder.where()
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper(whereWrapper)
    whereWrapper.build()
    return where.iterator()
}

/**
 *  查询构造器
 *  需要携带一个 Where 对象 用于拼接语句
 *  还需要一个 Entity 的 Class 用于获取列名
 */
class WhereWrapper<T, ID>(val where: Where<T, ID>, val entity: Class<*>) {

    // 操作序列
    val sequence = mutableListOf<Query<T>>()

    data class Query<T>(
        val query: Where<T, *>.() -> Unit,
        val type: QueryType = QueryType.LINK
    )

    enum class QueryType {
        LINK, NO_LINK
    }

    fun eval(type: QueryType = QueryType.NO_LINK, query: Where<T, *>.() -> Unit) {
        sequence.add(Query(query, type))
    }


    infix fun KProperty<*>.eq(value: Any) = sequence.add(
        Query({ eq(this@eq.getColumnName(entity), value) })
    )

    infix fun KProperty<*>.`==`(value: Any) = sequence.add(
        Query({ eq(this@`==`.getColumnName(entity), value) })
    )

    infix fun KProperty<*>.ne(value: Any) = sequence.add(
        Query({ ne(this@ne.getColumnName(entity), value) })
    )

    infix fun KProperty<*>.`!=`(value: Any) = sequence.add(
        Query({ ne(this@`!=`.getColumnName(entity), value) })
    )

    // between
    infix fun KProperty<*>.between(value: Pair<*, *>) = sequence.add(
        Query({ between(this@between.getColumnName(entity), value.first, value.second) })
    )

    infix fun KProperty<*>.between(value: ClosedRange<*>) = sequence.add(
        Query({ between(this@between.getColumnName(entity), value.start, value.endInclusive) })
    )

    // ge >=
    infix fun KProperty<*>.ge(value: Any) = sequence.add(
        Query({ ge(this@ge.getColumnName(entity), value) })
    )

    // gt >
    infix fun KProperty<*>.gt(value: Any) = sequence.add(
        Query({ gt(this@gt.getColumnName(entity), value) })
    )

    // in
    infix fun KProperty<*>.`in`(value: Collection<*>) = sequence.add(
        Query({ `in`(this@`in`.getColumnName(entity), value) })
    )

    // notIn
    infix fun KProperty<*>.notIn(value: Collection<*>) = sequence.add(
        Query({ notIn(this@notIn.getColumnName(entity), value) })
    )

    // isNull
    fun KProperty<*>.isNull() = sequence.add(
        Query({ isNull(this@isNull.getColumnName(entity)) })
    )

    // isNotNull
    fun KProperty<*>.isNotNull() = sequence.add(
        Query({ isNotNull(this@isNotNull.getColumnName(entity)) })
    )

    // le <=
    infix fun KProperty<*>.le(value: Any) = sequence.add(
        Query({ le(this@le.getColumnName(entity), value) })
    )

    // lt <
    infix fun KProperty<*>.lt(value: Any) = sequence.add(
        Query({ lt(this@lt.getColumnName(entity), value) })
    )

    // like
    infix fun KProperty<*>.like(value: Any) = sequence.add(
        Query({ like(this@like.getColumnName(entity), value) })
    )

    // not
    fun KProperty<*>.not() = sequence.add(
        Query({ not() }, QueryType.NO_LINK)
    )

    // and
    fun and() = sequence.add(
        Query({ and() }, QueryType.NO_LINK)
    )

    // or
    fun or() = sequence.add(
        Query({ or() }, QueryType.NO_LINK)
    )

    fun build() {
        // 除了第一个以外都自动填充 AND
        // 如果下一个元素是 NO_LINK 则不填充
        sequence.forEachIndexed { index, it ->
            if (index == 0 || sequence.getOrNull(index - 1)?.type != QueryType.NO_LINK) {
                if (index > 0 && it.type == QueryType.LINK) {
                    where.and()
                }
                it.query.invoke(where)
            } else {
                it.query.invoke(where)
            }
        }
    }


}

/**
 *  读取列名
 *
 *  忙乎了一天的枫师父
 *  换了个写法终于兼容了
 */
fun <T> KProperty<T>.getColumnName(owner: Class<*>): String {
    val mutableMap = EasyORM.tablesColumn[owner] ?: error("Is not a Table - $owner")
    val tableColumn = mutableMap[this.name] ?: error("Is not a Column - $owner - ${this.name}")
    return tableColumn.columnNameProcessed
}

/**
 *  两种读取列名的别名
 */
infix fun <T> KProperty<T>.column(owner: Class<*>): String {
    return this.getColumnName(owner)
}

infix fun <T> KProperty<T>.from(owner: Class<*>): String {
    return this.getColumnName(owner)
}

