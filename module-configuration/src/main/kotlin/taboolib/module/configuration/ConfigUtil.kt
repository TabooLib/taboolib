@file:Isolated
package taboolib.module.configuration

import taboolib.common.Isolated
import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path).getKeys(false).forEach { key ->
        try {
            map[key as K] = get("${path}.${key}") as V
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
    return map
}