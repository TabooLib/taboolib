package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common5.Coerce
import taboolib.module.kether.*
import taboolib.module.kether.Kether.expects
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
            Coerce.toInteger(it)
        })
    }

    internal object Parser {

        val types = TypeTo.values().map { it.name.toLowerCase() }.toTypedArray()

        @KetherParser(["type"])
        fun parser() = scriptParser {
            try {
                it.mark()
                ActionTypeTo(TypeTo.valueOf(it.expects(*types).toUpperCase()), it.next(ArgTypes.ACTION))
            } catch (ex: Throwable) {
                it.reset()
                ActionType(it.nextToken())
            }
        }
    }
}