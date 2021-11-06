package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionType {

    class ActionType(val any: String) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(any.inferType())
        }

        override fun toString(): String {
            return "ActionType(any=$any)"
        }
    }

    class ActionTypeTo(val to: TypeTo, val action: ParsedAction<*>) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            return frame.newFrame(action).run<Any>().thenApply { to.transfer(it) }
        }

        override fun toString(): String {
            return "ActionTypeTo(action=$action, to=$to)"
        }
    }

    enum class TypeTo(val transfer: (Any) -> Any) {

        INT({
            Coerce.toInteger(it)
        }),

        LONG({
            Coerce.toLong(it)
        }),

        FLOAT({
            Coerce.toFloat(it)
        }),

        DOUBLE({
            Coerce.toDouble(it)
        }),

        BOOLEAN({
            Coerce.toBoolean(it)
        })
    }

    internal object Parser {

        val types = TypeTo.values().map { it.name.lowercase(Locale.getDefault()) }.toTypedArray()

        @KetherParser(["type"])
        fun parser() = scriptParser {
            try {
                it.mark()
                ActionTypeTo(TypeTo.valueOf(it.expects(*types).uppercase(Locale.getDefault())), it.next(ArgTypes.ACTION))
            } catch (ex: Throwable) {
                it.reset()
                ActionType(it.nextToken())
            }
        }
    }
}