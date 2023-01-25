package taboolib.expansion.ioc.serialization.impl

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.bukkit.Material
import org.bukkit.util.Vector
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.expansion.ioc.serialization.SerializeFunction
import taboolib.library.xseries.parseToMaterial
import java.lang.reflect.Type

open class SerializationFunctionGson : SerializeFunction {

    open var gson = GsonBuilder().apply {
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

    override val name: String = "Gson"

    override fun serialize(data: Any): String {
        return gson.toJson(data)
    }

    override fun deserialize(data: Any, target: Class<*>, type: Type): Any? {
        return gson.fromJson(data.toString(), type)
    }

    companion object {

        @Awake(LifeCycle.CONST)
        fun init() {
            val data = SerializationFunctionGson()
            SerializationManager.function[data.name] = data
        }

    }

}