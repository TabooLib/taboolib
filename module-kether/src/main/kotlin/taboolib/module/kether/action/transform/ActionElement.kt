package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common5.Coerce
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
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

        override fun toString(): String {
            return "SizeOf(array=$array)"
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

        override fun toString(): String {
            return "ElementOf(index=$index, array=$array)"
        }
    }

    companion object {

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