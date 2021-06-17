package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTell(val message: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return context.newFrame(message).run<Any>().thenAccept {
            val viewer = (context.context() as ScriptContext).sender ?: throw RuntimeException("No sender selected.")
            viewer.sendMessage(it.toString().trimIndent().replace("@sender", viewer.name))
        }
    }

    override fun toString(): String {
        return "ActionTell(message=$message)"
    }

    companion object {

        @KetherParser(["tell", "send", "message"])
        fun parser() = ScriptParser.parser {
            ActionTell(it.next(ArgTypes.ACTION))
        }
    }
}