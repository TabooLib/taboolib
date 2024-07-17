package taboolib.expansion.ioc

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.expansion.ioc.annotation.Component
import java.util.*

object IndexReader {

    fun getIndexId(instance: Any): String {
        val preset = UUID.randomUUID().toString()
        val annotation = instance::class.java.getAnnotation(Component::class.java) ?: return preset
        if (annotation.singleton) {
            return "${instance::class.java.name}_Singleton"
        }
        val id = annotation.index
        if (id == "null") {
            return preset
        }
        val getter = instance.getProperty<Any>(id, findToParent = true, isStatic = false)?.toString() ?: return preset
        return getter
    }

}