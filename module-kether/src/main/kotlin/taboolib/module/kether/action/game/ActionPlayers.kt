package taboolib.module.kether.action.game

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.common.platform.onlinePlayers
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPlayers() : QuestAction<List<String>>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<List<String>> {
        return CompletableFuture.completedFuture(onlinePlayers().map { it.name }.toList())
    }

    override fun toString(): String {
        return "ActionPlayers()"
    }

    companion object {

        @KetherParser(["players"])
        fun parser() = ScriptParser.parser {
            ActionPlayers()
        }
    }
}