package taboolib.module.kether.action.loop

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common5.util.Coerce
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.script
import java.util.concurrent.CompletableFuture


class ActionWhile(val condition: ParsedAction<*>, val action: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        fun process() {
            context.newFrame(condition).run<Any>().thenApply {
                if (Coerce.toBoolean(it)) {
                    context.newFrame(action).run<Any>().thenApply {
                        if (context.script().breakLoop) {
                            context.script().breakLoop = false
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

    companion object {

        @KetherParser(["while"])
        fun parser() = ScriptParser.parser {
            ActionWhile(it.next(ArgTypes.ACTION), it.run {
                expect("then")
                next(ArgTypes.ACTION)
            })
        }
    }
}