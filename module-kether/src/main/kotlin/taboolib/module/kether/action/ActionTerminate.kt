package taboolib.module.kether.action

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.ScriptService
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionTerminate : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        ScriptService.INSTANCE.terminateQuest(context.context() as ScriptContext)
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionTerminate()"
    }

    companion object {

        @KetherParser(["exit", "stop", "terminate"])
        fun parser() = ScriptParser.parser {
            ActionTerminate()
        }
    }
}