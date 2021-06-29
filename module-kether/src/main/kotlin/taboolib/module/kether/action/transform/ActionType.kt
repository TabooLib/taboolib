package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common5.Coerce
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.inferType
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionType {

    class ActionType(val any: String) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(any.inferType())
        }

        override fun toString(): String {
            return "ActionType(any=$any)"
        }
    }

    class ActionTypeTo(val to: TypeTo, val action: ParsedAction<*>) : QuestAction<Any>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any> {
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

    companion object {

        val types = TypeTo.values().map { it.name.toLowerCase() }.toTypedArray()

        @KetherParser(["type"])
        fun parser() = ScriptParser.parser {
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