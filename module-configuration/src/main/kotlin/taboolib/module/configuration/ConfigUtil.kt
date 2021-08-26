@file:Isolated

package taboolib.module.configuration

import taboolib.common.Isolated
import taboolib.common.util.Location
import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
fun <K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = mutableMapOf<K, V>()
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

fun ConfigurationSection.getLocation(path: String): Location? {
    getConfigurationSection(path)?.let { section ->
        return Location(
            section.getString("world"),
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z"),
            section.getDouble("pitch").toFloat(),
            section.getDouble("yaw").toFloat()
        )
    } ?: return null
}

fun ConfigurationSection.setLocation(path: String, location: Location) {
    createSection(path).apply {
        set("world", location.world)
        set("x", location.x)
        set("y", location.y)
        set("z", location.z)
        set("pitch", location.pitch)
        set("yaw", location.yaw)
    }
}