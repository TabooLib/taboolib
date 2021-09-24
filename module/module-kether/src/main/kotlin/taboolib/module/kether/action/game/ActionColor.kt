package taboolib.module.kether.action.game

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.chat.colored
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionColor(val source: ParsedAction<*>) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        return frame.newFrame(source).run<Any>().thenApply {
            it.toString().trimIndent().colored()
        }
    }

    internal object Parser {

        @KetherParser(["color", "colored"])
        fun parser() = scriptParser {
            ActionColor(it.next(ArgTypes.ACTION))
        }
    }
}