package taboolib.module.kether.action.loop

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.script
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionBreak : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        context.script().breakLoop = true
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionBreak()"
    }

    companion object {

        @KetherParser(["break"])
        fun parser() = ScriptParser.parser {
            ActionBreak()
        }
    }
}