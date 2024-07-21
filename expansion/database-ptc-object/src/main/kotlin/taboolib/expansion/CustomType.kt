package taboolib.expansion

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
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

    /** 对应 SQL 类型 */
    val typeSQL: ColumnTypeSQL
        get() = ColumnTypeSQL.TEXT

    /** 对应 SQLite 类型 */
    val typeSQLite: ColumnTypeSQLite
        get() = ColumnTypeSQLite.TEXT

    /** 长度 */
    val length: Int
        get() = 512

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