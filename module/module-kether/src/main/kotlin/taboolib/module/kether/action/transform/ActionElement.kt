package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.common5.cint
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionElement
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionElement {

    class SizeOf(val array: ParsedAction<*>) : ScriptAction<Int>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Int> {
            return frame.run(array).thenApply {
                when (it) {
                    is Collection<*> -> it.size
                    is Array<*> -> it.size
                    else -> it.toString().length
                }
            }
        }
    }

    class ElementOf(val index: ParsedAction<*>, val array: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()
            frame.run(index).int { index ->
                frame.run(array).thenApply {
                    future.complete(when (it) {
                        is Collection<*> -> it.toList().getOrNull(index)
                        is Array<*> -> it.toList().getOrNull(index)
                        else -> it.toString().getOrNull(index)
                    })
                }
            }
            return future
        }
    }

    object Parser {

        /**
         * size &array
         */
        @KetherParser(["size", "length"])
        fun parser0() = scriptParser {
            SizeOf(it.next(ArgTypes.ACTION))
        }

        /**
         * elem 1 in &array
         */
        @KetherParser(["elem", "element"])
        fun parser1() = scriptParser {
            ElementOf(it.nextParsedAction(), it.run {
                it.expects("in", "of")
                it.next(ArgTypes.ACTION)
            })
        }
    }
}