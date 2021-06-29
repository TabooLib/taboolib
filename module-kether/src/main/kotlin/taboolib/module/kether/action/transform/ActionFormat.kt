package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common5.Coerce
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionFormat(val date: ParsedAction<*>, val format: String) : QuestAction<String>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<String> {
        return frame.newFrame(date).run<Any>().thenApply {
            DateFormatUtils.format(Coerce.toLong(it), format)
        }
    }

    companion object {

        /**
         * format *date with "yyyy-MM-dd HH:mm"
         */
        @KetherParser(["format"])
        fun parser() = ScriptParser.parser {
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