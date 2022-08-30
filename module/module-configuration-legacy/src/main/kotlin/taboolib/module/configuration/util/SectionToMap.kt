@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                map[key as K] = section.get(key) as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}

fun <K, V> ConfigurationSection.getMap(path: String, transform: (String, Any) -> Pair<K, V>): Map<K, V> {
    return getMap<Any, Any>(path).map { transform(it.key.toString(), it.value) }.toMap()
}