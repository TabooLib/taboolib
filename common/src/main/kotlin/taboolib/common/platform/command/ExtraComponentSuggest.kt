@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.function.allWorlds
import taboolib.common.platform.function.onlinePlayers

/**
 * 创建参数补全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggest(suggest: () -> List<String>?): CommandComponentDynamic {
    return suggestion<ProxyCommandSender> { _, _ -> suggest() }
}

/**
 * 创建一个不检查的参数不全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggestUncheck(suggest: () -> List<String>?): CommandComponentDynamic {
    return suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> suggest() }
}

/**
 * 创建参数补全（仅布尔值）
 */
fun CommandComponentDynamic.suggestBoolean(): CommandComponentDynamic {
    return suggest { listOf("true", "false", "t", "f") }
}

/**
 * 创建参数补全（仅在线玩家名称）
 *
 * @param suggest 额外建议
 */
fun CommandComponentDynamic.suggestPlayers(suggest: List<String> = emptyList()): CommandComponentDynamic {
    return suggest {
        val el = onlinePlayers().map { it.name }.toMutableList()
        el += suggest
        el
    }
}

/**
 * 创建参数补全（仅世界名称）
 *
 * @param suggest 额外建议
 */
fun CommandComponentDynamic.suggestWorlds(suggest: List<String> = emptyList()): CommandComponentDynamic {
    return suggest {
        val el = allWorlds().toMutableList()
        el += suggest
        el
    }
}