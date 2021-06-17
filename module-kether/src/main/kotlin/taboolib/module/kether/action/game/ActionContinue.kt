package taboolib.module.kether.action.game

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionContinue : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        val s = context.context() as ScriptContext
        s.listener?.complete(null)
        s.listener = null
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionContinue()"
    }

    companion object {

        @KetherParser(["continue"])
        fun parser() = ScriptParser.parser {
            ActionContinue()
        }
    }
}