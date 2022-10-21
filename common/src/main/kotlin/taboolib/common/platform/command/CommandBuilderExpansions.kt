package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.allWorlds
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 获取 int 类型参数
 *
 * @param offset 参数偏移量
 * @return 参数值
 */
fun <T> CommandContext<T>.int(offset: Int): Int {
    return argument(offset).toInt()
}

/**
 * 获取 int 类型参数
 *
 * @param index 参数索引
 * @return 参数值
 */
fun <T> CommandContext<T>.intAt(index: Int): Int {
    return get(index).toInt()
}

/**
 * 获取 double 类型参数
 *
 * @param offset 参数偏移量
 * @return 参数值
 */
fun <T> CommandContext<T>.double(offset: Int): Double {
    return argument(offset).toDouble()
}

/**
 * 获取 double 类型参数
 *
 * @param index 参数索引
 * @return 参数值
 */
fun <T> CommandContext<T>.doubleAt(index: Int): Double {
    return get(index).toDouble()
}

/**
 * 获取 boolean 类型参数
 *
 * @param offset 参数偏移量
 * @return 参数值
 */
fun <T> CommandContext<T>.bool(offset: Int): Boolean {
    return argument(offset).toBooleanStrict()
}

/**
 * 获取 boolean 类型参数
 *
 * @param index 参数索引
 * @return 参数值
 */
fun <T> CommandContext<T>.boolAt(index: Int): Boolean {
    return get(index).toBoolean()
}

/**
 * 获取 ProxyPlayer 类型参数
 *
 * @param offset 参数偏移量
 * @return 参数值
 */
fun <T> CommandContext<T>.player(offset: Int): ProxyPlayer {
    return onlinePlayers().first { it.name == argument(offset) }
}

/**
 * 获取 ProxyPlayer 类型参数
 *
 * @param index 参数索引
 * @return 参数值
 */
fun <T> CommandContext<T>.playerAt(index: Int): ProxyPlayer {
    return onlinePlayers().first { it.name == get(index) }
}

/**
 * 获取 Location 类型参数
 *
 * @param offset 参数偏移量
 * @param detailed 是否为详细模式（包含 yaw、pitch）
 * @return 参数值
 */
fun <T> CommandContext<T>.location(offset: Int, detailed: Boolean = false): Location {
    val sender = sender()
    val world = if (sender is ProxyPlayer && argument(offset) == "~") sender.world else argument(offset)
    val x = if (sender is ProxyPlayer && argument(offset + 1) == "~") sender.location.x else argument(offset + 1).toDoubleOrNull() ?: 0.0
    val y = if (sender is ProxyPlayer && argument(offset + 2) == "~") sender.location.y else argument(offset + 2).toDoubleOrNull() ?: 0.0
    val z = if (sender is ProxyPlayer && argument(offset + 3) == "~") sender.location.z else argument(offset + 3).toDoubleOrNull() ?: 0.0
    if (detailed) {
        val yaw = if (sender is ProxyPlayer && argument(offset + 4) == "~") sender.location.yaw else argument(offset + 4).toDoubleOrNull() ?: 0.0
        val pitch = if (sender is ProxyPlayer && argument(offset + 5) == "~") sender.location.pitch else argument(offset + 5).toDoubleOrNull() ?: 0.0
        return Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
    }
    return Location(world, x, y, z)
}

/**
 * 获取 Location 类型参数
 *
 * @param index 参数索引
 * @param detailed 是否为详细模式（包含 yaw、pitch）
 * @return 参数值
 */
fun <T> CommandContext<T>.locationAt(index: Int, detailed: Boolean = false): Location {
    val sender = sender()
    val world = if (sender is ProxyPlayer && get(index) == "~") sender.world else get(index)
    val x = if (sender is ProxyPlayer && get(index + 1) == "~") sender.location.x else get(index + 1).toDoubleOrNull() ?: 0.0
    val y = if (sender is ProxyPlayer && get(index + 2) == "~") sender.location.y else get(index + 2).toDoubleOrNull() ?: 0.0
    val z = if (sender is ProxyPlayer && get(index + 3) == "~") sender.location.z else get(index + 3).toDoubleOrNull() ?: 0.0
    if (detailed) {
        val yaw = if (sender is ProxyPlayer && get(index + 4) == "~") sender.location.yaw else get(index + 4).toDoubleOrNull() ?: 0.0
        val pitch = if (sender is ProxyPlayer && get(index + 5) == "~") sender.location.pitch else get(index + 5).toDoubleOrNull() ?: 0.0
        return Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
    }
    return Location(world, x, y, z)
}

/**
 * 创建参数补全
 *
 * @param suggest 补全表达式
 */
fun CommandBuilder.CommandComponentDynamic.suggest(suggest: () -> List<String>?) {
    suggestion<ProxyCommandSender> { _, _ -> suggest() }
}

/**
 * 创建参数补全（仅布尔值）
 */
fun CommandBuilder.CommandComponentDynamic.suggestBoolean() {
    suggestion<ProxyCommandSender> { _, _ -> listOf("true", "false") }
}

/**
 * 创建参数补全（仅在线玩家名称）
 */
fun CommandBuilder.CommandComponentDynamic.suggestPlayers() {
    suggestion<ProxyCommandSender> { _, _ -> onlinePlayers().map { it.name } }
}

/**
 * 创建参数约束（仅 int 类型）
 */
fun CommandBuilder.CommandComponentDynamic.restrictInt() {
    restrict<ProxyCommandSender> { _, _, args -> args.toIntOrNull() != null }
}

/**
 * 创建参数约束（仅 double 类型）
 */
fun CommandBuilder.CommandComponentDynamic.restrictDouble() {
    restrict<ProxyCommandSender> { _, _, args -> args.toDoubleOrNull() != null }
}

/**
 * 创建参数约束（仅 boolean 类型）
 */
fun CommandBuilder.CommandComponentDynamic.restrictBoolean() {
    restrict<ProxyCommandSender> { _, _, args -> args.toBooleanStrictOrNull() != null }
}

/**
 * 定义 Location 参数
 *
 * @param detailed 是否为详细模式（包含 yaw、pitch）
 */
fun CommandBuilder.CommandComponent.location(detailed: Boolean = false, dynamic: CommandBuilder.CommandComponentDynamic.() -> Unit) {
    dynamic("world") {
        suggestion<ProxyCommandSender> { sender, _ -> worlds(sender) }
        dynamic("x") {
            suggestion<ProxyPlayer>(uncheck = true) { sender, _ -> listOf(format(sender.location.x), "~") }
            dynamic("y") {
                suggestion<ProxyPlayer>(uncheck = true) { sender, _ -> listOf(format(sender.location.y), "~") }
                dynamic("z") {
                    suggestion<ProxyPlayer>(uncheck = true) { sender, _ -> listOf(format(sender.location.z), "~") }
                    if (detailed) {
                        dynamic("yaw") {
                            suggestion<ProxyPlayer>(uncheck = true) { sender, _ -> listOf(format(sender.location.y), "~") }
                            dynamic("pitch") {
                                suggestion<ProxyPlayer>(uncheck = true) { sender, _ -> listOf(format(sender.location.z), "~") }
                                dynamic(this)
                            }
                        }
                    } else {
                        dynamic(this)
                    }
                }
            }
        }
    }
}

private fun format(value: Double): String {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toString()
}

private fun worlds(sender: ProxyCommandSender): List<String> {
    return allWorlds().toMutableList().also {
        if (sender is ProxyPlayer) {
            it += "~"
        }
    }
}