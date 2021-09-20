package taboolib.module.kether.action

import taboolib.common.platform.function.warning
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionWarning(val message: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(message).run<Any>().thenAccept {
            warning(it.toString().trimIndent())
        }
    }

    internal object Parser {

        @KetherParser(["warn", "warning"])
        fun parser() = scriptParser {
            ActionWarning(it.next(ArgTypes.ACTION))
        }
    }
}