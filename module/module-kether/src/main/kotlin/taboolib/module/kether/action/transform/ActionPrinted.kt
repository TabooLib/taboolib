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
        return frame.newFrame(date).run<Any>().thenApply { date ->
            frame.newFrame(separator).run<Any>().thenApply { separator ->
                date.toString().printed(separator.toString())
            }.join()
        }
    }

    internal object Parser {

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