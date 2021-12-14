package taboolib.module.configuration

import java.io.Serializable
import kotlin.reflect.KProperty

class ConfigLiteralDelegate<T : Serializable> internal constructor(val config: Configuration, val path: String) {

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Configuration, property: KProperty<*>): T {
        return try {
            config[path] as T
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not get property \"${property.name}\" in \"$path\"").initCause(ex)
        }
    }

    operator fun setValue(thisRef: Configuration, property: KProperty<*>, value: T) {
        try {
            config[path] = value
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not set property \"${property.name}\" to \"$path\"").initCause(ex)
        }
    }
}
