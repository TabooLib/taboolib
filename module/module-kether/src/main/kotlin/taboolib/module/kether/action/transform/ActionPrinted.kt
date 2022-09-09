package taboolib.module.kether.action.transform

import taboolib.common5.util.printed
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPrinted(val date: ParsedAction<*>, val separator: ParsedAction<*>) : ScriptAction<List<String>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
        val future = CompletableFuture<List<String>>()
        frame.run(date).thenApply { date ->
            frame.run(separator).thenApply { separator ->
                future.complete(date.toString().printed(separator.toString()))
            }
        }
        return future
    }

    object Parser {

        /**
         * printed *xxx by "_"
         */
        @KetherParser(["printed"])
        fun parser() = scriptParser {
            ActionPrinted(it.nextParsedAction(), try {
                it.mark()
                it.expects("by", "with")
                it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
                literalAction("_")
            })
        }
    }
}