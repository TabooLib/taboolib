package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionListen(val operator: EventOperator<*>, val value: ParsedAction<*>) : ScriptAction<Void>() {

    @Suppress("UNCHECKED_CAST")
    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return CompletableFuture<Void>().also { future ->
            val s = frame.script()
            s.listener = future
            frame.addClosable(Closables.listening<Any>(operator.event.java) {
                s.event = it
                s.eventOperator = operator
                frame.newFrame(value).run<Any>()
            })
        }
    }

    override fun toString(): String {
        return "ActionListen(operator=$operator, value=$value)"
    }

    companion object {

        @KetherParser(["listen", "on"])
        fun parser() = scriptParser {
            val name = it.nextToken()
            val event = Kether.getEventOperator(name) ?: throw KetherError.NOT_EVENT.create(name)
            it.expect("then")
            ActionListen(event, it.next(ArgTypes.ACTION))
        }
    }
}