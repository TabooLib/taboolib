package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionEvent(val key: String, val symbol: Symbol, val value: ParsedAction<*>) : QuestAction<Any?>() {

    enum class Symbol {

        GET, SET
    }

    @Suppress("UNCHECKED_CAST")
    override fun process(context: QuestContext.Frame): CompletableFuture<Any?> {
        val s = (context.context() as ScriptContext)
        val event = s.event
        val eventOperator = s.eventOperator
        if (event == null || eventOperator == null) {
            throw RuntimeException("No event selected.")
        }
        return if (symbol == Symbol.SET) {
            context.newFrame(value).run<Any>().thenApply {
                eventOperator.write(key, event, it)
            }
        } else {
            CompletableFuture.completedFuture(eventOperator.read(key, event))
        }
    }

    override fun toString(): String {
        return "ActionEvent(key='$key', symbol=$symbol, value=$value)"
    }

    companion object {

        @KetherParser(["event"])
        fun parser() = ScriptParser.parser {
            val key = it.nextToken()
            try {
                it.mark()
                it.expect("to")
                ActionEvent(key, Symbol.SET, it.next(ArgTypes.ACTION))
            } catch (ex: Throwable) {
                it.reset()
                ActionEvent(key, Symbol.GET, ParsedAction.noop<Any>())
            }
        }
    }
}