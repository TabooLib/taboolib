package taboolib.expansion.ioc.database

interface IOCDatabase {

    fun init(clazz: Class<*>, source: String): IOCDatabase
    fun getDataAll(): Map<String, Any?>

    fun getData(key: String): Any?

    fun saveData(key: String, data: Any): Boolean

    fun saveDao()
    fun serialize(data: Any): String
    fun deserialize(key: String, target: Class<*>): Any
}