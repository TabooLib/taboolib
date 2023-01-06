@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponentDynamic

/**
 * 创建参数约束（仅 int 类型）
 */
fun CommandComponentDynamic.restrictInt(): CommandComponentDynamic {
    return restrict<ProxyCommandSender> { _, _, args -> args.toIntOrNull() != null }
}

/**
 * 创建参数约束（仅 double 类型）
 */
fun CommandComponentDynamic.restrictDouble(): CommandComponentDynamic {
    return restrict<ProxyCommandSender> { _, _, args -> args.toDoubleOrNull() != null }
}

/**
 * 创建参数约束（仅 boolean 类型）
 */
fun CommandComponentDynamic.restrictBoolean(): CommandComponentDynamic {
    return restrict<ProxyCommandSender> { _, _, args -> args.toBooleanStrictOrNull() != null }
}