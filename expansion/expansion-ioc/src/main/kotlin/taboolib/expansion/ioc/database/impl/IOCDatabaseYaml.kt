package taboolib.expansion.ioc.database.impl

import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.createLocal

open class IOCDatabaseYaml : IOCDatabase {

    var config: Configuration? = null

    override fun init(clazz: Class<*>): IOCDatabase {
        config = createLocal("data-ioc/${clazz.name}/data.yml", type = Type.YAML)
        return this
    }

    override fun getDataAll(): Map<String, Any?> {
        if (config?.getKeys(false)?.size == 0) {
            return mapOf()
        }
        return config?.getKeys(false)?.associate { it to getData(it) } ?: mapOf()
    }

    override fun getData(key: String): String? {
        return config?.getString(key)
    }

    override fun writeData(key: String, data: Any): Boolean {
        config?.set(key, SerializationManager.serialize(data))
        return true
    }

    override fun saveData(key: String) {
        config?.saveToFile()
    }

    override fun saveDatabase() {
        config?.saveToFile()
    }

    override fun resetDatabase() {
        config?.clear()
    }

}