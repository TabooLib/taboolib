package taboolib.module.kether.action.transform

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common5.Coerce
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionFormat(val date: ParsedAction<*>, val format: ParsedAction<*>) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        return frame.newFrame(date).run<Any>().thenApply { date ->
            frame.newFrame(format).run<Any>().thenApply { format ->
                DateFormatUtils.format(Coerce.toLong(date), format.toString())
            }.join()
        }
    }

    internal object Parser {

        /**
         * format *date with "yyyy-MM-dd HH:mm"
         */
        @KetherParser(["format"])
        fun parser() = scriptParser {
            ActionFormat(
                it.nextParsedAction(), try {
                    it.mark()
                    it.expects("by", "with")
                    it.nextParsedAction()
                } catch (ignored: Exception) {
                    it.reset()
                    literalAction("yyyy/MM/dd HH:mm")
                }
            )
        }
    }
}