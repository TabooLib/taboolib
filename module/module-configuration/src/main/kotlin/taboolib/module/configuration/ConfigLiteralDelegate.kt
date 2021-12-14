package taboolib.module.configuration

import taboolib.module.configuration.Configuration.Companion.getTypedObject
import taboolib.module.configuration.Configuration.Companion.setObject
import java.io.Serializable
import kotlin.reflect.KProperty

class ConfigLiteralDelegate<T : Serializable> internal constructor(
    val config: Configuration,
    val path: String
) {

    operator fun getValue(thisRef: T?, property: KProperty<*>): T {
        return try {
            config.getTypedObject(path)
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not get property \"${property.name}\" in \"$path\"").initCause(ex)
        }
    }

    operator fun setValue(thisRef: T?, property: KProperty<*>, value: T) {
        try {
            config.setObject(path, value)
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not set property \"${property.name}\" to \"$path\"").initCause(ex)
        }
    }
}
