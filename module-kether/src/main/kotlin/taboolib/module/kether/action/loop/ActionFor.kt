package taboolib.module.kether.action.loop

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.script
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
class ActionFor(val key: String, val values: ParsedAction<*>, val action: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        context.newFrame(values).run<Any>().thenApply {
            fun process(cur: Int, i: List<Any>) {
                if (cur < i.size) {
                    context.variables()[key] = i[cur]
                    context.newFrame(action).run<Any>().thenApply {
                        if (context.script().breakLoop) {
                            context.script().breakLoop = false
                            future.complete(null)
                        } else {
                            process(cur + 1, i)
                        }
                    }
                } else {
                    context.variables().remove(key)
                    future.complete(null)
                }
            }
            when (it) {
                is Collection<*> -> {
                    process(0, it.map { i -> i as Any }.toList())
                }
                is Array<*> -> {
                    process(0, it.map { i -> i as Any }.toList())
                }
                else -> {
                    process(0, listOf(it))
                }
            }
        }
        return future
    }

    companion object {

        /**
         * for i in players then {  }
         * for i in range 1 to 10 then {  }
         */
        @KetherParser(["for"])
        fun parser() = ScriptParser.parser {
            ActionFor(it.nextToken(), it.run {
                expect("in")
                next(ArgTypes.ACTION)
            }, it.run {
                expect("then")
                next(ArgTypes.ACTION)
            })
        }
    }
}