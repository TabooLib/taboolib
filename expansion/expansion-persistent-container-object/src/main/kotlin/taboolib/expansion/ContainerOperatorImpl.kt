package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Where
import javax.sql.DataSource

/**
 * TabooLib
 * taboolib.expansion.ContainerOperatorImpl
 *
 * @author 坏黑
 * @since 2023/3/29 13:29
 */
class ContainerOperatorImpl(override val table: Table<*, *>, override val dataSource: DataSource) : ContainerOperator() {

    override fun <T> getOne(type: Class<T>, where: Where.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        return table.select(dataSource) {
            limit(1)
            where(where)
        }.firstOrNull { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun <T> get(type: Class<T>, where: Where.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        return table.select(dataSource) { where(where) }.map { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun <T> findOne(type: Class<T>, id: Any, where: Where.() -> Unit): T? {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        return table.select(dataSource) {
            limit(1)
            where(name eq id.value())
            where(where)
        }.firstOrNull { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun <T> find(type: Class<T>, id: Any, where: Where.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        return table.select(dataSource) {
            where(name eq id.value())
            where(where)
        }.map { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun <T> sort(type: Class<T>, row: String, limit: Int, where: Where.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        return table.select(dataSource) {
            order(row)
            limit(limit)
            where(where)
        }.map { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun <T> sortDescending(type: Class<T>, row: String, limit: Int, where: Where.() -> Unit): List<T> {
        val typeClass = AnalyzedClass.of(type)
        return table.select(dataSource) {
            order(row, true)
            limit(limit)
            where(where)
        }.map { typeClass.createInstance(typeClass.read(this)) }
    }

    override fun update(data: Any, where: Where.() -> Unit) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isFinal }) {
            error("No mutable field found.")
        }
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        val value = typeClass.getPrimaryMemberValue(data)
        // 检查是否存在
        if (table.find(dataSource) {
                limit(1)
                where(name eq value.value())
                where(where)
            }) {
            // 更新数据
            table.update(dataSource) {
                where(name eq value.value())
                where(where)
                // 获取可变字段
                typeClass.members.filter { !it.isFinal }.forEach { member ->
                    set(member.name, typeClass.getValue(data, member).value())
                }
            }
        } else {
            insert(listOf(data))
        }
    }

    override fun updateByKey(data: Any) {
        val typeClass = AnalyzedClass.of(data::class.java)
        if (typeClass.members.none { !it.isFinal }) {
            error("No mutable field found.")
        }
        update(data) {
            typeClass.members.filter { it.isKey }.forEach { member ->
                member.name eq typeClass.getValue(data, member).value()
            }
        }
    }

    override fun insert(dataList: List<Any>) {
        if (dataList.isEmpty()) {
            return
        }
        val typeClass = AnalyzedClass.of(dataList.first().javaClass)
        table.insert(dataSource, typeClass.members.map { it.name }) {
            dataList.forEach { data ->
                values(typeClass.members.map { member -> typeClass.getValue(data, member).value() })
            }
        }
    }

    override fun <T> has(type: Class<T>, id: Any, where: Where.() -> Unit): Boolean {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        return table.find(dataSource) {
            limit(1)
            where(name eq id.value())
            where(where)
        }
    }

    override fun has(where: Where.() -> Unit): Boolean {
        return table.find(dataSource) {
            limit(1)
            where(where)
        }
    }

    override fun <T> delete(type: Class<T>, id: Any, where: Where.() -> Unit) {
        val typeClass = AnalyzedClass.of(type)
        val name = typeClass.primaryMemberName ?: error("No primary id found.")
        table.delete(dataSource) {
            where(name eq id.value())
            where(where)
        }
    }
}