package taboolib.expansion.ioc.linker

import taboolib.expansion.ioc.IOCReader
import taboolib.expansion.ioc.annotation.Component
import java.util.concurrent.ConcurrentHashMap

inline fun <reified T : Any?> linkedIOCSingleton(): IOCSingleton {
    if (T::class.java.getAnnotation(Component::class.java)?.singleton != true) {
        println("Please understand what you are doing. You are operating a class not specified by TabooIOC for singleton processing: ${T::class.java}")
    }
    return IOCSingleton(T::class.java)
}

class IOCSingleton(
    val dataType: Class<*>,
) {
    val IOC by lazy {
        IOCReader.dataMap.getOrPut(dataType.name) { ConcurrentHashMap() }
    }

    fun getId(): String {
        return "${dataType.name}_Singleton"
    }

    fun set(data: Any) {
        IOC[getId()] = data
    }

    fun get(): Any? {
        return IOC[getId()]
    }

    fun remove() {
        IOC.clear()
    }

}