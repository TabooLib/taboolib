package taboolib.expansion.ioc.typeread

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.expansion.ioc.annotation.Component
import taboolib.expansion.ioc.event.GetTypeReaderEvent
import taboolib.expansion.ioc.typeread.impl.TypeReaderSingletonObject
import java.util.UUID

object TypeReadManager {

    val typeReader = HashMap<String, TypeRead>()

    fun getReader(clazz: Class<*>): TypeRead {
        val save = typeReader[clazz.name]
        if (save != null) {
            return save
        }
        typeReader.values.forEach {
            if (it.type.isAssignableFrom(clazz)) {
                return it
            }
        }
        val event = GetTypeReaderEvent(clazz)
        event.call()
        return event.reader ?: (typeReader[Any::class.java.name] ?: TypeReaderSingletonObject())
    }

    fun getIndexId(instance: Any): String {
        val annotation = instance::class.java.getAnnotation(Component::class.java)
            ?: return UUID.randomUUID().toString()
        val id = annotation.index
        if (id == "null") {
            return UUID.randomUUID().toString()
        }
        return instance.getProperty<Any>(id, findToParent = true, isStatic = false)?.toString()
            ?: return UUID.randomUUID().toString()
    }

}