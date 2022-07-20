package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection
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
inline operator fun <R, reified T> ConfigurationSection.getValue(thisRef: R, property: KProperty<*>): T {
    val value = get(property.name)
    if (value is ConfigurationSection) {
        return getObject(property.name, true)
    }
    return get(property.name) as? T ?: error("wrong type for config delegate: ${property.name}")
}

inline operator fun <R, reified T : Any> ConfigurationSection.setValue(thisRef: R, property: KProperty<*>, value: T) {
    when(value) {
        is String, is Long,
        is Short, is Int,
        is Double, is Float,
        is Boolean, is Collection<*>,
        is ConfigurationSection, is Map<*, *> -> set(property.name, value)
        else -> setObject(property.name, value)
    }
}