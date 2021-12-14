package taboolib.module.configuration

import taboolib.module.configuration.Configuration.Companion.getTypedObject
import taboolib.module.configuration.Configuration.Companion.setObject
import kotlin.reflect.KProperty

class ConfigLiteralDelegate<T : Any> internal constructor(
    val config: Configuration,
    val path: String
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return try {
            config.getTypedObject(path)
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not get property \"${property.name}\" in \"$path\"").initCause(ex)
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        try {
            config.setObject(path, value)
        } catch (ex: Throwable) {
            throw IllegalAccessException("Can not set property \"${property.name}\" to \"$path\"").initCause(ex)
        }
    }
}
