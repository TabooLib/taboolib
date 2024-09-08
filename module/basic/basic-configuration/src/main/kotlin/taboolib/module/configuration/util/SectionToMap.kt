package taboolib.module.configuration.util

import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
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