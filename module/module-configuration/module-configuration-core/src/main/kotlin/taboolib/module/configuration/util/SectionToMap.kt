@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection

fun <V> ConfigurationSection.getMapAs(path: String = "", transfer: Any.() -> V): Map<String, V> {
    return getMap<String, Any>(path).map { it.key to transfer(it.value) }.toMap()
}

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String = ""): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                map[key as K] = section[key] as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}