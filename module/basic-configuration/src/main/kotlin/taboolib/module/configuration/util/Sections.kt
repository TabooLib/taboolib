package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration

/**
 * 将任意 [Map] 或 [Configuration] 转换为 [Map<String, Any?>]
 */
fun Any?.asMap(): Map<String, Any?> = when (this) {
    is Map<*, *> -> entries.associate { it.key.toString() to it.value }
    is ConfigurationSection -> getValues(false)
    else -> emptyMap()
}

fun <V> ConfigurationSection.map(transform: (String, Any) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(it, get(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.map(node: String, transform: (String, Any) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(it, get("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <V> ConfigurationSection.mapValue(transform: (Any) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(get(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.mapValue(node: String, transform: (Any) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(get("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <V> ConfigurationSection.mapSection(transform: (ConfigurationSection) -> V): Map<String, V> {
    return getKeys(false).associateWith { transform(getConfigurationSection(it) ?: error("$it is null")) }
}

fun <V> ConfigurationSection.mapSection(node: String, transform: (ConfigurationSection) -> V): Map<String, V> {
    return getConfigurationSection(node)?.getKeys(false)?.associateWith { transform(getConfigurationSection("$node.$it") ?: error("$it is null")) } ?: emptyMap()
}

fun <T> ConfigurationSection.mapListAs(path: String, transform: (Map<String, Any?>) -> T): MutableList<T> {
    return getMapList(path).map { transform(it.asMap()) }.toMutableList()
}