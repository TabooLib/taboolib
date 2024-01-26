package taboolib.expansion.ioc.database.impl

import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.createLocal
import java.io.File
import java.util.concurrent.ConcurrentHashMap

open class IOCDatabaseMultipleYaml : IOCDatabase {

    val config = ConcurrentHashMap<String, Configuration>()

    var clazz: String = ""

    val files = ArrayList<File>()

    fun getConfig(key: String): Configuration {
        return config.getOrPut(key) {
            createLocal("data-ioc/${clazz}/${key}.yml", type = Type.YAML)
        }
    }

    fun loadConfig() {
        files.forEach {
            val key = it.name.substring(0, it.name.lastIndexOf("."))
            config[key] = Configuration.loadFromFile(it, type = Type.YAML)
        }
    }

    fun loadFile(file: File) {
        if (file.isFile) {
            files.add(file)
        } else {
            file.listFiles()?.forEach {
                loadFile(it)
            }
        }
    }

    override fun init(clazz: Class<*>): IOCDatabase {
        this.clazz = clazz.name
        // 不在这里进行构建
        // config = createLocal("data-ioc/${clazz.name}/data.yml", type = Type.YAML)
        loadFile(File(getDataFolder(), "data-ioc/${clazz.name}/"))
        loadConfig()
        return this
    }

    override fun getDataAll(): Map<String, Any?> {
        return config.toMap().mapNotNull { it.key to getData(it.key) }.toMap()
    }

    override fun getData(key: String): String? {
        return getConfig(key).getString("data")
    }

    override fun writeData(key: String, data: Any): Boolean {
        val conf = getConfig(key)
        conf["data"] = SerializationManager.serialize(data)
        return true
    }

    override fun saveData(key: String) {
        getConfig(key).saveToFile()
        config.remove(key)
    }

    override fun saveDatabase() {
        config.forEach { (_, u) ->
            u.saveToFile()
        }
    }

    override fun resetDatabase() {
        return
    }
}