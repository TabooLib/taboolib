package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Configuration.Companion.getObject
import taboolib.module.configuration.Configuration.Companion.setObject
import kotlin.reflect.KProperty

/**
 * taboolib.module.configuration.util.Delegate
 * TabooLib
 * Configuration 代理
 *
 * @author 寒雨
 * @since 2022/4/29 0:32
 **/

fun config(config: Configuration, path: String? = null) = Delegate(config, path)

class Delegate(val config: Configuration, val path: String?) {
    inline operator fun <R, reified T> getValue(thisRef: R, property: KProperty<*>): T {
        val value = config[property.name]
        if (value is ConfigurationSection) {
            return config.getObject(property.name, true)
        }
        return  config[path ?: property.name] as? T ?: error("wrong type for config delegate: ${path ?: property.name}")
    }

    inline operator fun <R, reified T : Any> setValue(thisRef: R, property: KProperty<*>, value: T) {
        when(value) {
            is String, is Long,
            is Short, is Int,
            is Double, is Float,
            is Boolean, is Collection<*>,
            is ConfigurationSection, is Map<*, *> -> config[path ?: property.name] = value
            else -> config.setObject(path ?: property.name, value)
        }
    }
}