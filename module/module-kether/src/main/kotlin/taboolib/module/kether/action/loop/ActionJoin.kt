package taboolib.module.kether.action.loop

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionJoin(val source: List<ParsedAction<*>>, val separator: ParsedAction<*>) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        frame.newFrame(separator).run<Any>().thenAccept { separator ->
            val array = ArrayList<Any>()
            fun process(cur: Int) {
                if (cur < source.size) {
                    frame.newFrame(source[cur]).run<Any>().thenApply {
                        array.add(it)
                        if (frame.script().breakLoop) {
                            frame.script().breakLoop = false
                            future.complete(array.joinToString(separator.toString()))
                        } else {
                            process(cur + 1)
                        }
                    }
                } else {
                    future.complete(array.joinToString(separator.toString()))
                }
            }
            process(0)
        }
        return future
    }

    object Parser {

        /**
         * join [ 1 2 3 ] by -
         * join [ a b c ] with -
         */
        @KetherParser(["join"])
        fun parser() = scriptParser {
            val source = it.next(ArgTypes.listOf(ArgTypes.ACTION))
            ActionJoin(
                source, try {
                    it.mark()
                    it.expects("by", "with")
                    it.nextParsedAction()
                } catch (ignored: Exception) {
                    it.reset()
                    literalAction("")
                }
            )
        }
    }
}