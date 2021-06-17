package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionListen(val operator: EventOperator<*>, val value: ParsedAction<*>) : QuestAction<Void>() {

    @Suppress("UNCHECKED_CAST")
    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return CompletableFuture<Void>().also { future ->
            val s = (context.context() as ScriptContext)
            s.listener = future
            context.addClosable(Closables.listening<Any>(operator.event.java) {
                s.event = it
                s.eventOperator = operator
                context.newFrame(value).run<Any>()
            })
        }
    }

    override fun toString(): String {
        return "ActionListen(operator=$operator, value=$value)"
    }

    companion object {

        @KetherParser(["listen", "on"])
        fun parser() = ScriptParser.parser {
            val name = it.nextToken()
            val event = Kether.getEventOperator(name) ?: throw KetherError.NOT_EVENT.create(name)
            it.expect("then")
            ActionListen(event, it.next(ArgTypes.ACTION))
        }
    }
}