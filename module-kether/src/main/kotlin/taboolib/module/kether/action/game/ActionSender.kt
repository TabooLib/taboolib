package taboolib.module.kether.action.game

import taboolib.common.util.isConsole
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSender : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        val sender = frame.script().sender
        return if (sender.isConsole()) {
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