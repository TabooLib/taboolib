package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
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
            return frame.newFrame(array).run<Any>().thenApply {
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
            frame.newFrame(index).run<Any>().thenApply { index ->
                frame.newFrame(array).run<Any>().thenApply {
                    future.complete(when (it) {
                        is Collection<*> -> it.toList().getOrNull(Coerce.toInteger(index))
                        is Array<*> -> it.toList().getOrNull(Coerce.toInteger(index))
                        else -> it.toString().getOrNull(Coerce.toInteger(index))
                    })
                }
            }
            return future
        }
    }

    internal object Parser {

        /**
         * size &array
         */
        @KetherParser(["size", "length"])
        fun parser0() = scriptParser {
            SizeOf(it.next(ArgTypes.ACTION))
        }

        /**
         * elem *1 in &array
         */
        @KetherParser(["elem", "element"])
        fun parser1() = scriptParser {
            ElementOf(it.next(ArgTypes.ACTION), it.run {
                it.expects("in", "of")
                it.next(ArgTypes.ACTION)
            })
        }
    }
}