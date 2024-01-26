package taboolib.expansion.ioc.database

interface IOCDatabase {

    // 初始化容器
    fun init(clazz: Class<*>): IOCDatabase

    // 获取所有数据 反序列化前的数据通常是String
    fun getDataAll(): Map<String, Any?>

    // 获取某个数据
    fun getData(key: String): Any?

    // 讲数据写入缓存
    fun writeData(key: String, data: Any): Boolean

    // 保存某个数据
    fun saveData(key: String)

    // 保存所有数据
    fun saveDatabase()

    // 刷新数据源 每次writeData前会执行一次
    fun resetDatabase()

}