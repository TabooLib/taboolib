package taboolib.module.configuration

import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = mutableMapOf<K, V>()
    getConfigurationSection(path).getKeys(false).forEach { key ->
        try {
            map[key as K] = get("${path}.${key}") as V
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
    return map
}