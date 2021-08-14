package taboolib.module.kether.action.loop

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionJoin(val source: List<ParsedAction<*>>, val separator: String) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        val array = ArrayList<Any>()
        fun process(cur: Int) {
            if (cur < source.size) {
                frame.newFrame(source[cur]).run<Any>().thenApply {
                    array.add(it)
                    if (frame.script().breakLoop) {
                        frame.script().breakLoop = false
                        future.complete(array.joinToString(separator))
                    } else {
                        process(cur + 1)
                    }
                }
            } else {
                future.complete(array.joinToString(separator))
            }
        }
        process(0)
        return future
    }

    internal object Parser {

        /**
         * join [ *1 *2 *3 ] by -
         * join [ *a *b *c ] with -
         */
        @KetherParser(["join"])
        fun parser() = scriptParser {
            val source = it.next(ArgTypes.listOf(ArgTypes.ACTION))
            ActionJoin(
                source, try {
                    it.mark()
                    it.expects("by", "with")
                    it.nextToken()
                } catch (ignored: Exception) {
                    it.reset()
                    " "
                }
            )
        }
    }
}