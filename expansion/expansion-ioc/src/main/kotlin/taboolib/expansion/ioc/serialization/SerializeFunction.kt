package taboolib.expansion.ioc.serialization

import java.lang.reflect.Type

interface SerializeFunction {

    val name: String

    fun serialize(data: Any): String

    fun deserialize(data: Any, target: Class<*>, type: Type): Any?
}