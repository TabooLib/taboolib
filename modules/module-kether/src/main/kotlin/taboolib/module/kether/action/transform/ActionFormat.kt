package taboolib.module.kether.action.transform

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionFormat(val date: ParsedAction<*>, val format: String) : ScriptAction<String>() {

    override fun run(frame: ScriptFrame): CompletableFuture<String> {
        return frame.newFrame(date).run<Any>().thenApply {
            DateFormatUtils.format(Coerce.toLong(it), format)
        }
    }

    internal object Parser {

        /**
         * format *date with "yyyy-MM-dd HH:mm"
         */
        @KetherParser(["format"])
        fun parser() = scriptParser {
            ActionFormat(
                it.next(ArgTypes.ACTION), try {
                    it.mark()
                    it.expects("by", "with")
                    it.nextToken()
                } catch (ignored: Exception) {
                    it.reset()
                    "yyyy/MM/dd HH:mm"
                }
            )
        }
    }
}