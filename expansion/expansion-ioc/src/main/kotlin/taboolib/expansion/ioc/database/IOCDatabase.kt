package taboolib.expansion.ioc.database

import java.lang.reflect.Type

interface IOCDatabase {

    fun init(clazz: Class<*>): IOCDatabase
    fun getDataAll(): Map<String, Any?>

    fun getData(key: String): Any?

    fun saveData(key: String, data: Any): Boolean

    fun saveDatabase()

    fun resetDatabase()

}