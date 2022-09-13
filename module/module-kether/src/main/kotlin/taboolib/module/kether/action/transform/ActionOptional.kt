package taboolib.module.kether.action.transform

import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
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
        frame.run(value).thenAccept {
            if (it != null) {
                future.complete(it)
            } else {
                frame.run(elseOf).thenAccept { elseOf -> future.complete(elseOf) }
            }
        }
        return future
    }

    object Parser {

        /**
         * optional null else 123
         */
        @KetherParser(["optional"])
        fun parser() = scriptParser {
            ActionOptional(it.nextParsedAction(), it.run {
                it.expect("else")
                it.nextParsedAction()
            })
        }
    }
}