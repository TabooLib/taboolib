package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionListen(val operator: Class<*>, val value: ParsedAction<*>) : ScriptAction<Void>() {

    @Suppress("UNCHECKED_CAST")
    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return CompletableFuture<Void>().also { future ->
            val s = frame.script()
            s.listener = future
            frame.addClosable(Closables.listening<Any>(operator) {
                s.event = it
                frame.newFrame(value).run<Any>()
            })
        }
    }

    internal object Parser {

        @KetherParser(["listen", "on"])
        fun parser1() = scriptParser {
            val name = it.nextToken()
            val event = Kether.getEvent(name) ?: throw KetherError.NOT_EVENT.create(name)
            it.expect("then")
            ActionListen(event, it.next(ArgTypes.ACTION))
        }

        @KetherParser(["event"])
        fun parser2() = scriptParser {
            actionNow {
                CompletableFuture.completedFuture(script().event)
            }
        }
    }
}