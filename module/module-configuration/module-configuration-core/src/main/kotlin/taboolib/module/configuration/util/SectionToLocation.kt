@file:Isolated

package taboolib.module.configuration.util

import taboolib.common.Isolated
import taboolib.common.util.Location
import taboolib.library.configuration.ConfigurationSection

fun ConfigurationSection.setLocation(path: String, location: Location) {
    set(path, mapOf(
        "world" to location.world,
        "x" to location.x,
        "y" to location.y,
        "z" to location.z,
        "pitch" to location.pitch,
        "yaw" to location.yaw
    ))
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