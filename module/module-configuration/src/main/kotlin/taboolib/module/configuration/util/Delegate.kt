package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Configuration.Companion.getObject
import taboolib.module.configuration.Configuration.Companion.setObject
import taboolib.module.configuration.Type
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
    when(T::class) {
        String::class, Long::class,
        Short::class, Int::class,
        Double::class, Float::class,
        Boolean::class, Collection::class,
        ConfigurationSection::class, Map::class -> set(property.name, value)
        else -> setObject(property.name, value)
    }
}