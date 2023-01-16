package taboolib.ioc.database.impl

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.bukkit.Material
import org.bukkit.util.Vector
import taboolib.library.xseries.parseToMaterial

open class IOCDatabaseYamlGson : IOCDatabaseYaml() {

    var gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().apply {
        registerTypeAdapter(
            Vector::class.java,
            JsonSerializer<Vector> { a, _, _ ->
                JsonPrimitive("${a.x},${a.y},${a.z}")
            }
        )
        registerTypeAdapter(
            Vector::class.java,
            JsonDeserializer { a, _, _ ->
                a.asString.split(",").run { Vector(this[0].toDouble(), this[1].toDouble(), this[2].toDouble()) }
            }
        )
        registerTypeAdapter(
            Material::class.java,
            JsonSerializer<Material> { a, _, _ ->
                JsonPrimitive(a.name)
            }
        )
        registerTypeAdapter(
            Material::class.java,
            JsonDeserializer { a, _, _ ->
                a.asString.parseToMaterial()
            }
        )
    }.create()!!

    override fun serialize(data: Any): String {
        return gson.toJson(data)
    }

    override fun deserialize(key: String, target: Class<*>): Class<*> {
        return gson.fromJson(config?.getString(key), target::class.java)
    }
}