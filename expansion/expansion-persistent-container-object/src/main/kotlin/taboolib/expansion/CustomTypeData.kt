package taboolib.expansion

import taboolib.module.database.ColumnOptionSQLite
import taboolib.module.database.ColumnTypeSQL

/** Ray_Hughes **/
interface CustomTypeData {

    val sqlType: ColumnTypeSQL?

    val sqlLiteType: ColumnOptionSQLite?

    /** 判断是否可用 */
    fun isThis(obj: Any): Boolean

    fun isThisByClass(clazz: Class<*>): Boolean

    /** 序列化-存入数据库 */
    fun serialize(obj: Any): Any

    /** 反序列化-从数据库取出 */
    fun deserialize(obj: Any): Any

    fun register() {
        CustomObjectType.types[this::class.java] = this
    }
}
