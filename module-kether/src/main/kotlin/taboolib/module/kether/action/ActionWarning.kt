package taboolib.module.kether.action

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common.platform.warning
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionWarning(val message: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return context.newFrame(message).run<Any>().thenAccept {
            warning(it.toString().trimIndent())
        }
    }

    override fun toString(): String {
        return "ActionWarning(message=$message)"
    }

    companion object {

        @KetherParser(["warn", "warning"])
        fun parser() = ScriptParser.parser {
            ActionWarning(it.next(ArgTypes.ACTION))
        }
    }
}