package taboolib.module.kether.action.game

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.common.platform.ProxyConsole
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSender : QuestAction<String>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<String> {
        val sender = (context.context() as ScriptContext).sender
        return if (sender is ProxyConsole) {
            CompletableFuture.completedFuture("console")
        } else {
            CompletableFuture.completedFuture(sender?.name ?: "null")
        }
    }

    override fun toString(): String {
        return "ActionSender()"
    }

    companion object {

        @KetherParser(["sender"])
        fun parser() = ScriptParser.parser {
            ActionSender()
        }
    }
}