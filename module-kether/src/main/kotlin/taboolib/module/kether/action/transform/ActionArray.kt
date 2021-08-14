package taboolib.module.kether.action.transform

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * TabooLibKotlin
 * taboolib.module.kether.action.transform.ActionArray
 *
 * @author sky
 * @since 2021/1/30 9:26 下午
 */
class ActionArray(val list: List<ParsedAction<*>>) : ScriptAction<List<Any>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<Any>> {
        val future = CompletableFuture<List<Any>>()
        run(frame, future, 0, list, ArrayList())
        return future
    }

    fun run(frame: ScriptFrame, future: CompletableFuture<List<Any>>, cur: Int, i: List<ParsedAction<*>>, array: ArrayList<Any>) {
        if (cur < i.size) {
            frame.newFrame(i[cur]).run<Any>().thenApply {
                array.add(it)
                run(frame, future, cur + 1, i, array)
            }
        } else {
            future.complete(array)
        }
    }

    internal object Parser {

        /**
         * set a to array [ *1 *2 *3 ]
         */
        @KetherParser(["array"])
        fun parser() = scriptParser {
            ActionArray(it.next(ArgTypes.listOf(ArgTypes.ACTION)))
        }
    }
}