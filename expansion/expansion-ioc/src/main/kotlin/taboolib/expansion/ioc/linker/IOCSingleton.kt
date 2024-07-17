package taboolib.expansion.ioc.linker

import taboolib.common.util.unsafeLazy
import taboolib.expansion.ioc.IOCReader
import taboolib.expansion.ioc.annotation.Component
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.database.impl.IOCDatabaseYaml
import java.util.concurrent.ConcurrentHashMap

inline fun <reified T : Any?> linkedIOCSingleton(): IOCSingleton {
    if (T::class.java.getAnnotation(Component::class.java)?.singleton != true) {
        println("Please understand what you are doing. You are operating a class not specified by TabooIOC for singleton processing: ${T::class.java}")
    }
    return IOCSingleton(T::class.java)
}

class IOCSingleton(val dataType: Class<*>) {

    val ioc: ConcurrentHashMap<String, Any> by unsafeLazy {
        IOCReader.dataMap.getOrPut(dataType.name) { ConcurrentHashMap() }
    }

    val database: IOCDatabase by unsafeLazy {
        IOCReader.databaseMap.getOrPut(dataType.name) { IOCDatabaseYaml() }
    }

    fun getId(): String {
        return "${dataType.name}_Singleton"
    }

    fun set(data: Any) {
        ioc[getId()] = data
    }

    fun get(): Any? {
        return ioc[getId()]
    }

    fun remove() {
        ioc.clear()
    }
}