package taboolib.module.kether.action.transform

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionOptional
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionOptional(val value: ParsedAction<*>, val elseOf: ParsedAction<*>) : ScriptAction<Any>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        val future = CompletableFuture<Any>()
        frame.newFrame(value).run<Any>().thenApply {
            if (it != null) {
                future.complete(it)
            } else {
                frame.newFrame(elseOf).run<Any>().thenApply { elseOf ->
                    future.complete(elseOf)
                }
            }
        }
        return future
    }

    internal object Parser {

        /**
         * optional null else 123
         */
        @KetherParser(["optional"])
        fun parser() = scriptParser {
            ActionOptional(it.next(ArgTypes.ACTION), it.run {
                it.expect("else")
                it.next(ArgTypes.ACTION)
            })
        }
    }
}