package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionSplit
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionOptional(val value: ParsedAction<*>, val elseOf: ParsedAction<*>) : QuestAction<Any>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
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

    override fun toString(): String {
        return "ActionOptional(value=$value, elseOf=$elseOf)"
    }

    companion object {

        /**
         * optional null else 123
         */
        @KetherParser(["optional"])
        fun parser() = ScriptParser.parser {
            ActionOptional(it.next(ArgTypes.ACTION), it.run {
                it.expect("else")
                it.next(ArgTypes.ACTION)
            })
        }
    }
}