package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.ExecuteContext
import taboolib.common.platform.command.component.SuggestContext
import taboolib.common.platform.function.allWorlds
import taboolib.common.platform.function.onlinePlayers

/**
 * 创建参数补全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggest(suggest: SuggestContext<ProxyCommandSender>.() -> List<String>?): CommandComponentDynamic {
    return suggestion<ProxyCommandSender> { sender, ctx -> suggest(SuggestContext(sender, ctx)) }
}

/**
 * 创建一个不检查的参数不全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggestUncheck(suggest: SuggestContext<ProxyCommandSender>.() -> List<String>?): CommandComponentDynamic {
    return suggestion<ProxyCommandSender>(uncheck = true) { sender, ctx -> suggest(SuggestContext(sender, ctx)) }
}

/**
 * 创建参数补全（仅布尔值）
 */
fun CommandComponentDynamic.suggestBoolean(): CommandComponentDynamic {
    return suggest { listOf("true", "false", "t", "f", "1", "0") }
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