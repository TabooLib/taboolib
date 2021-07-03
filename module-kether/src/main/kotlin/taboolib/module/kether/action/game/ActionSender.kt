package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyConsole
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSender : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        val sender = frame.script().sender
        return if (sender is ProxyConsole) {
            CompletableFuture.completedFuture("console")
        } else {
            CompletableFuture.completedFuture(sender?.name ?: "null")
        }
    }

    internal object Parser {

        @KetherParser(["sender"])
        fun parser() = scriptParser {
            ActionSender()
        }
    }
}