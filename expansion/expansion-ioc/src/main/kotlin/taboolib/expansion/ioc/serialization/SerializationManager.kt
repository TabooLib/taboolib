package taboolib.expansion.ioc.serialization

import taboolib.expansion.ioc.annotation.Component
import taboolib.expansion.ioc.event.SerializationGetFunctionEvent
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

object SerializationManager {

    val function = ConcurrentHashMap<String, SerializeFunction>()

    fun getFunction(data: Class<*>): SerializeFunction {
        return if (data.isAnnotationPresent(Component::class.java)) {
            val clazz = data.getAnnotation(Component::class.java)
            val target = function[clazz.function]
            if (target == null) {
                val event = SerializationGetFunctionEvent(data, clazz.function, function["Gson"]!!)
                return event.function
            } else {
                target
            }
        } else {
            function["Gson"]!!
        }
    }

    fun serialize(data: Any): String {
        return getFunction(data::class.java).serialize(data)
    }

    fun deserialize(data: Any, target: Class<*>, type: Type): Any? {
        return getFunction(target).deserialize(data, target, type)
    }
}