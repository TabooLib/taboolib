@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated

/**
 * 获取选项并转换为整形
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器，或者选项不存在，或者选项不是整型
 */
fun <T> CommandContext<T>.optionInt(vararg id: String): Int {
    return option(*id)?.toInt() ?: error("Option $id not found.")
}

/**
 * 获取选项并转换为整形
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器
 */
fun <T> CommandContext<T>.optionIntOrNull(vararg id: String): Int? {
    return option(*id)?.toIntOrNull()
}

/**
 * 获取选项并转换为双精度浮点数
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器，或者选项不存在，或者选项不是布尔值
 */
fun <T> CommandContext<T>.optionDouble(vararg id: String): Double {
    return option(*id)?.toDouble() ?: error("Option $id not found.")
}

/**
 * 获取选项并转换为双精度浮点数
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器
 */
fun <T> CommandContext<T>.optionDoubleOrNull(vararg id: String): Double? {
    return option(*id)?.toDoubleOrNull()
}

/**
 * 获取选项并转换为单精度浮点数
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器，或者选项不存在，或者选项不是布尔值
 */
fun <T> CommandContext<T>.optionFloat(vararg id: String): Float {
    return option(*id)?.toFloat() ?: error("Option $id not found.")
}

/**
 * 获取选项并转换为单精度浮点数
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器
 */
fun <T> CommandContext<T>.optionFloatOrNull(vararg id: String): Float? {
    return option(*id)?.toFloatOrNull()
}

/**
 * 获取选项并转换为布尔值
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器，或者选项不存在
 */
fun <T> CommandContext<T>.optionBoolean(vararg id: String): Boolean {
    val value = option(*id) ?: error("Option $id not found.")
    return value.equals("true", true) || value.equals("t", true)
}

/**
 * 获取选项并转换为布尔值
 *
 * @param id 选项名称
 * @return 选项值
 * @throws IllegalStateException 如果当前命令不支持新的命令解析器
 */
fun <T> CommandContext<T>.optionBooleanOrNull(vararg id: String): Boolean? {
    val value = option(*id) ?: return null
    return value.equals("true", true) || value.equals("t", true)
}