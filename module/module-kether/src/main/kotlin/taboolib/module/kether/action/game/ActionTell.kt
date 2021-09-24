package taboolib.module.kether.action.game

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTell(val message: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(message).run<Any>().thenAccept {
            val viewer = frame.script().sender ?: error("No sender selected.")
            viewer.sendMessage(it.toString().trimIndent().replace("@sender", viewer.name))
        }
    }

    internal object Parser {

        @KetherParser(["tell", "send", "message"])
        fun parser() = scriptParser {
            ActionTell(it.next(ArgTypes.ACTION))
        }
    }
}