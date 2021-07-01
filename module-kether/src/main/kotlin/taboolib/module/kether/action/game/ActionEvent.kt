package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionEvent(val key: String, val symbol: Symbol, val value: ParsedAction<*>) : ScriptAction<Any?>() {

    enum class Symbol {

        GET, SET
    }

    @Suppress("UNCHECKED_CAST")
    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val s = frame.script()
        val event = s.event
        val eventOperator = s.eventOperator
        if (event == null || eventOperator == null) {
            error("No event selected.")
        }
        return if (symbol == Symbol.SET) {
            frame.newFrame(value).run<Any>().thenApply {
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
        fun parser() = scriptParser {
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