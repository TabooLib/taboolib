package taboolib.module.kether.action.supplier

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.LoadError
import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.module.kether.Kether
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionDate(val type: Type, val format: String? = null) : QuestAction<Any>() {

    enum class Type(val get: () -> Long) {

        TIME({
            System.currentTimeMillis()
        }),

        YEAR({
            java.time.LocalDateTime.now().year.toLong()
        }),

        MONTH({
            java.time.LocalDateTime.now().month.value.toLong()
        }),

        DAY_OF_YEAR({
            java.time.LocalDateTime.now().dayOfYear.toLong()
        }),

        DAY_OF_MONTH({
            java.time.LocalDateTime.now().dayOfMonth.toLong()
        }),

        DAY_OF_WEEK({
            java.time.LocalDateTime.now().dayOfWeek.value.toLong()
        }),

        HOUR({
            java.time.LocalDateTime.now().hour.toLong()
        }),

        MINUTE({
            java.time.LocalDateTime.now().minute.toLong()
        }),

        SECOND({
            java.time.LocalDateTime.now().second.toLong()
        });

        fun toParser() = ScriptParser.parser {
            ActionDate(this)
        }
    }

    override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
        return if (format != null) {
            CompletableFuture.completedFuture(DateFormatUtils.format(System.currentTimeMillis(), format))
        } else {
            CompletableFuture.completedFuture(type.get())
        }
    }

    override fun toString(): String {
        return "ActionDate(type=$type, format=$format)"
    }

    companion object {

        @KetherParser
        fun parser() {
            Kether.addAction(arrayOf("year", "years"), Type.YEAR.toParser())
            Kether.addAction(arrayOf("month", "months"), Type.MONTH.toParser())
            Kether.addAction(arrayOf("hour", "hours"), Type.HOUR.toParser())
            Kether.addAction(arrayOf("minute", "minutes"), Type.MINUTE.toParser())
            Kether.addAction(arrayOf("second", "seconds"), Type.SECOND.toParser())
        }

        /**
         * time
         * time as "yyyy-MM-dd HH:mm"
         */
        @KetherParser(["time", "date"])
        fun parserDate() = ScriptParser.parser {
            it.mark()
            val format = try {
                it.expect("as")
                it.nextToken()
            } catch (ignored: Exception) {
                it.reset()
                null
            }
            ActionDate(Type.TIME, format)
        }

        @KetherParser(["day", "days"])
        fun parserDays() = ScriptParser.parser {
            it.expects("of", "in")
            when (it.expects("year", "month", "week")) {
                "year" -> ActionDate(Type.DAY_OF_YEAR)
                "month" -> ActionDate(Type.DAY_OF_MONTH)
                "week" -> ActionDate(Type.DAY_OF_WEEK)
                else -> throw LoadError.UNHANDLED.create()
            }
        }
    }
}