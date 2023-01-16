package taboolib.expansion.ioc.database.impl

import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.module.configuration.*

open class IOCDatabaseYaml : IOCDatabase {

    var config: Configuration? = null

    override fun init(clazz: Class<*>, source: String): IOCDatabase {
        config = createLocal("data/bean/${clazz.name}/${source}.yml", type = Type.YAML)
        return this
    }

    override fun getDataAll(): Map<String, Any?> {
        return config?.getKeys(false)?.associate { it to config!![it] } ?: mapOf()
    }

    override fun getData(key: String): Any? {
        return config?.get(key)
    }

    override fun saveData(key: String, data: Any): Boolean {
        config?.set(key, serialize(data))
        return true
    }

    override fun saveDao() {
        config?.saveToFile()
    }

    override fun serialize(data: Any): String {
        return Configuration.serialize(data).toString()
    }

    override fun deserialize(key: String, target: Class<*>): Class<*> {
        return Configuration.deserialize(config?.getConfigurationSection(key)!!, target)
    }


}