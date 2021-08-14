package taboolib.module.kether.action.loop

import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

class ActionWhile(val condition: ParsedAction<*>, val action: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        fun process() {
            frame.newFrame(condition).run<Any>().thenApply {
                if (Coerce.toBoolean(it)) {
                    frame.newFrame(action).run<Any>().thenApply {
                        if (frame.script().breakLoop) {
                            frame.script().breakLoop = false
                            future.complete(null)
                        } else {
                            process()
                        }
                    }
                } else {
                    future.complete(null)
                }
            }
        }
        process()
        return future
    }

    internal object Parser {

        @KetherParser(["while"])
        fun parser() = scriptParser {
            ActionWhile(it.next(ArgTypes.ACTION), it.run {
                expect("then")
                next(ArgTypes.ACTION)
            })
        }
    }
}