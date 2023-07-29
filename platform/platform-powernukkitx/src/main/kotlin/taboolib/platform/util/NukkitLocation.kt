package taboolib.platform.util

import cn.nukkit.level.Location

fun Location.toCommonLocation(): taboolib.common.util.Location {
    return taboolib.common.util.Location(
        level.name, x, y, z, yaw.toFloat(),
        pitch.toFloat()
    )
}