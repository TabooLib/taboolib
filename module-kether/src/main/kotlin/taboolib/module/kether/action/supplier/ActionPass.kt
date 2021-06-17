package taboolib.module.kether.action.supplier

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionPass : QuestAction<String>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
        return CompletableFuture.completedFuture("")
    }

    override fun toString(): String {
        return "ActionPass()"
    }

    companion object {

        @KetherParser(["pass"])
        fun parser() = ScriptParser.parser {
            ActionPass()
        }
    }
}