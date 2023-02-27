@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated

/**
 * 根据节点名称获取输入参数并转换为整型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者参数不是整型
 */
fun <T> CommandContext<T>.int(id: String): Int {
    return get(id).toInt()
}

/**
 * 根据节点名称获取输入参数并转换为整型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.intOrNull(id: String): Int? {
    return getOrNull(id)?.toIntOrNull()
}

/**
 * 根据节点名称获取输入参数并转换为浮点型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者参数不是整型
 */
fun <T> CommandContext<T>.double(id: String): Double {
    return get(id).toDouble()
}

/**
 * 根据节点名称获取输入参数并转换为浮点型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.doubleOrNull(id: String): Double? {
    return getOrNull(id)?.toDoubleOrNull()
}

/**
 * 根据节点名称获取输入参数并转换为浮点型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者参数不是整型
 */
fun <T> CommandContext<T>.float(id: String): Float {
    return get(id).toFloat()
}

/**
 * 根据节点名称获取输入参数并转换为浮点型
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.floatOrNull(id: String): Float? {
    return getOrNull(id)?.toFloatOrNull()
}

/**
 * 根据节点名称获取输入参数并转换为布尔值
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者参数不是整型
 */
fun <T> CommandContext<T>.bool(id: String): Boolean {
    val value = get(id)
    return value.equals("true", true) || value.equals("1", true)
}

/**
 * 根据节点名称获取输入参数并转换为布尔值
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.boolOrNull(id: String): Boolean? {
    val value = getOrNull(id) ?: return null
    return value.equals("true", true) || value.equals("1", true)
}