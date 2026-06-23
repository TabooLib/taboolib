package taboolib.expansion

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.database.ColumnTypePostgreSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.ColumnTypeSQLite

/**
 * TabooLib
 * taboolib.expansion.CustomType
 *
 * @author Ray_Hughes, 坏黑
 * @since 2023/8/12 00:57
 */
interface CustomType {

    /** 类型 */
    val type: Class<*>

    /**
     * 集合元素类型。
     * 当 elementType != null 时，表示这是一个集合 CustomType，
     * 用于将整个集合字段（List/Set/Map）作为单列存储。
     * - type = 集合类型（如 List::class.java）
     * - elementType = 元素类型（如 XXXData::class.java）
     */
    val elementType: Class<*>?
        get() = null

    /** 对应 SQL 类型 */
    val typeSQL: ColumnTypeSQL
        get() = ColumnTypeSQL.TEXT

    /** 对应 SQLite 类型 */
    val typeSQLite: ColumnTypeSQLite
        get() = ColumnTypeSQLite.TEXT

    /** 对应 PostgreSQL 类型 */
    val typePostgreSQL: ColumnTypePostgreSQL
        get() = ColumnTypePostgreSQL.TEXT

    /** 长度 */
    val length: Int
        get() = 512

    /** PostgreSQL 长度 */
    val lengthPostgreSQL: Int
        get() = length

    /** 类型是否匹配 */
    fun match(value: Any): Boolean {
        return type.isInstance(value)
    }

    /** 类型是否匹配 */
    fun matchType(clazz: Class<*>): Boolean {
        return type.isAssignableFrom(clazz)
    }

    /** 序列化 */
    fun serialize(value: Any): Any {
        return Configuration.serialize(value, Type.FAST_JSON).toString()
    }

    /** 反序列化 */
    fun deserialize(value: Any): Any {
        return Configuration.deserialize(Configuration.loadFromString(value.toString()), Type.FAST_JSON, ignoreConstructor = true)
    }
}