package taboolib.expansion.ioc.database.impl

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.bukkit.Material
import org.bukkit.util.Vector
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.library.xseries.parseToMaterial
import taboolib.module.configuration.*

open class IOCDatabaseYaml : IOCDatabase {

    var config: Configuration? = null

    override fun init(clazz: Class<*>, source: String): IOCDatabase {
        config = createLocal("data-ioc/${clazz.name}/${source}.yml", type = Type.YAML)
        return this
    }

    override fun getDataAll(): Map<String, Any?> {
        if (config?.getKeys(false)?.size == 0) {
            return mapOf()
        }
        return config?.getKeys(false)?.associate { it to config!![it] } ?: mapOf()
    }

    override fun getData(key: String): String? {
        return config?.getString(key)
    }

    override fun saveData(key: String, data: Any): Boolean {
        config?.set(key, SerializationManager.serialize(data))
        return true
    }

    override fun saveDatabase() {
        config?.saveToFile()
    }

    override fun resetDatabase() {
        config?.clear()
    }

}