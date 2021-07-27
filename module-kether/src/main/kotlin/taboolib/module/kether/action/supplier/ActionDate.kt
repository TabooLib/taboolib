package taboolib.module.kether.action.supplier

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionDate(val type: Type, val format: String? = null) : ScriptAction<Any>() {

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

        fun toParser() = scriptParser {
            ActionDate(this)
        }
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        return if (format != null) {
            CompletableFuture.completedFuture(DateFormatUtils.format(System.currentTimeMillis(), format))
        } else {
            CompletableFuture.completedFuture(type.get())
        }
    }

    internal object Parser {

        @Awake(LifeCycle.LOAD)
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
        fun parserDate() = scriptParser {
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
        fun parserDays() = scriptParser {
            it.expects("of", "in")
            it.switch {
                case("year") {
                    ActionDate(Type.DAY_OF_YEAR)
                }
                case("month") {
                    ActionDate(Type.DAY_OF_MONTH)
                }
                case("week") {
                    ActionDate(Type.DAY_OF_WEEK)
                }
            }
        }
    }
}