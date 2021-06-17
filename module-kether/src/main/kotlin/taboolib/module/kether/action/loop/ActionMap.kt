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
class ActionMap(val key: String, val values: ParsedAction<*>, val action: ParsedAction<*>) : QuestAction<List<Any>>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<List<Any>> {
        val future = CompletableFuture<List<Any>>()
        val result = ArrayList<Any>()
        context.newFrame(values).run<Any>().thenApply {
            fun process(cur: Int, i: List<Any>) {
                if (cur < i.size) {
                    context.variables()[key] = i[cur]
                    context.newFrame(action).run<Any>().thenApply { map ->
                        if (map != null) {
                            result += map
                        }
                        if (context.script().breakLoop) {
                            context.script().breakLoop = false
                            context.variables().remove(key)
                            future.complete(result)
                        } else {
                            process(cur + 1, i)
                        }
                    }
                } else {
                    context.variables().remove(key)
                    future.complete(result)
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
        return future;
    }

    companion object {

        @KetherParser(["map"])
        fun parser() = ScriptParser.parser {
            ActionMap(it.nextToken(), it.run {
                expect("in")
                next(ArgTypes.ACTION)
            }, it.run {
                expect("with")
                next(ArgTypes.ACTION)
            })
        }
    }
}