@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.util.Location

/**
 * 获取坐标中的世界
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者原点世界不存在
 */
fun <T> CommandContext<T>.world(id: String = "world", origin: Location? = null): String {
    return if (get(id) == "~") (origin ?: player().location).world!! else get(id)
}

/**
 * 获取坐标中的世界
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.worldOrNull(id: String = "world", origin: Location? = null): String? {
    val world = getOrNull(id) ?: return null
    return if (world == "~") (origin ?: player().location).world else world
}

/**
 * 获取坐标中的 X
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.x(id: String = "x", origin: Location? = null): Double {
    return if (get(id).startsWith('~')) (origin ?: player().location).x + get(id).substring(1).double() else get(id).double()
}

/**
 * 获取坐标中的 Y
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.y(id: String = "y", origin: Location? = null): Double {
    return if (get(id).startsWith('~')) (origin ?: player().location).y + get(id).substring(1).double() else get(id).double()
}

/**
 * 获取坐标中的 Z
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.z(id: String = "z", origin: Location? = null): Double {
    return if (get(id).startsWith('~')) (origin ?: player().location).z + get(id).substring(1).double() else get(id).double()
}

/**
 * 获取坐标中的 YAW
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.yaw(id: String = "yaw", origin: Location? = null): Float {
    return if (get(id).startsWith('~')) (origin ?: player().location).yaw + get(id).substring(1).float() else get(id).float()
}

/**
 * 获取坐标中的 YAW
 *
 * @param origin 原点（默认为玩家位置）
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.yawOrNull(id: String = "yaw", origin: Location? = null): Float? {
    val yaw = getOrNull(id) ?: return null
    return if (yaw.startsWith('~')) (origin ?: player().location).yaw + yaw.substring(1).float() else yaw.float()
}

/**
 * 获取坐标中的 PITCH
 *
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.pitch(id: String = "pitch", origin: Location? = null): Float {
    return if (get(id).startsWith('~')) (origin ?: player().location).pitch + get(id).substring(1).float() else get(id).float()
}

/**
 * 获取坐标中的 PITCH
 *
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.pitchOrNull(id: String = "pitch", origin: Location? = null): Float? {
    val pitch = getOrNull(id) ?: return null
    return if (pitch.startsWith('~')) (origin ?: player().location).pitch + pitch.substring(1).float() else pitch.float()
}

/**
 * 获取坐标
 *
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.location(
    world: String = "world",
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    origin: Location? = null
): Location {
    return Location(world(world, origin), x(x, origin), y(y, origin), z(z, origin), yawOrNull(yaw, origin) ?: 0f, pitchOrNull(pitch, origin) ?: 0f)
}

/**
 * 获取不带世界参数的坐标，自动获取玩家所在的世界
 *
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者类型不匹配
 */
fun <T> CommandContext<T>.locationWithoutWorld(
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    origin: Location? = null
): Location {
    val pw = (origin ?: player().location).world
    return Location(pw, x(x, origin), y(y, origin), z(z, origin), yawOrNull(yaw, origin) ?: 0f, pitchOrNull(pitch, origin) ?: 0f)
}

private fun String.double(): Double {
    return toDoubleOrNull() ?: 0.0
}

private fun String.float(): Float {
    return toFloatOrNull() ?: 0.0f
}