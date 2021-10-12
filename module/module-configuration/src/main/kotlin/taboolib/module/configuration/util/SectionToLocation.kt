@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.common.util.Location
import taboolib.library.configuration.ConfigurationSection

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