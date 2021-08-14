package taboolib.platform.util

import cn.nukkit.level.Location

fun Location.toCommonLocation(): taboolib.common.util.Location {
    return taboolib.common.util.Location(level.name, x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)
}