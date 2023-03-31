package taboolib.expansion.ioc.serialization.impl

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.ioc.serialization.SerializationManager
import taboolib.expansion.ioc.serialization.SerializeFunction
import taboolib.library.xseries.parseToMaterial
import java.lang.reflect.Type
import java.util.*

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
        registerTypeAdapter(
            ItemStack::class.java,
            JsonSerializer<ItemStack> { src, typeOfSrc, context ->
                Gson().toJsonTree(src.serialize())
            }
        )
        registerTypeAdapter(
            ItemStack::class.java,
            JsonDeserializer { json, typeOfT, context ->
                ItemStack.deserialize(
                    Gson().fromJson(
                        json,
                        object : TypeToken<MutableMap<String, Any>>() {}.type
                    )
                )
            }
        )
        registerTypeAdapter(
            Location::class.java,
            JsonSerializer<Location> { a, _, _ ->
                JsonPrimitive(fromLocation(a))
            }
        )
        registerTypeAdapter(
            Location::class.java,
            JsonDeserializer { a, _, _ ->
                toLocation(a.asString)
            }
        )
        registerTypeAdapter(
            BlockFace::class.java,
            JsonSerializer<BlockFace> { a, _, _ ->
                JsonPrimitive(a.name)
            }
        )
        registerTypeAdapter(
            BlockFace::class.java,
            JsonDeserializer { a, _, _ ->
                BlockFace.valueOf(a.asString)
            }
        )
        registerTypeAdapter(
            OfflinePlayer::class.java,
            JsonSerializer<OfflinePlayer> { a, _, _ ->
                JsonPrimitive(a.uniqueId.toString())
            }
        )
        registerTypeAdapter(
            OfflinePlayer::class.java,
            JsonDeserializer { a, _, _ ->
                Bukkit.getOfflinePlayer(UUID.fromString(a.asString))
            }
        )

    }.create()!!

    private fun toLocation(source: String): Location {
        return source.replace("__", ".").split(",").run {
            Location(
                Bukkit.getWorld(get(0)),
                getOrElse(1) { "0" }.toDouble(),
                getOrElse(2) { "0" }.toDouble(),
                getOrElse(3) { "0" }.toDouble()
            )
        }
    }

    private fun fromLocation(location: Location): String {
        return "${location.world?.name},${location.x},${location.y},${location.z}".replace(".", "__")
    }

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