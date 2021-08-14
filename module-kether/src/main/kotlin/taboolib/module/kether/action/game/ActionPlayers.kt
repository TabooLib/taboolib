package taboolib.module.kether.action.game

import taboolib.common.platform.function.onlinePlayers
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/*
 * @author IzzelAliz
 */
class ActionPlayers : ScriptAction<List<String>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
        return CompletableFuture.completedFuture(onlinePlayers().map { it.name }.toList())
    }

    internal object Parser {

        @KetherParser(["players"])
        fun parser() = scriptParser {
            ActionPlayers()
        }
    }
}