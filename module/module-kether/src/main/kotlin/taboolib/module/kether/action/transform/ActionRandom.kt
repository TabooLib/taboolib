package taboolib.module.kether.action.transform

import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.actions.LiteralAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

/**
 * TabooLibKotlin
 * taboolib.module.kether.action.transform.ActionRandom
 *
 * @author sky
 * @since 2021/1/30 9:26 下午
 */
class ActionRandom(val from: ParsedAction<*>, val to: ParsedAction<*>, val action: ParsedAction<*>? = null) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        if (action == null) {
            return frame.newFrame(from).run<Any>().thenApply { from ->
                frame.newFrame(to).run<Any>().thenApply { to ->
                    if (from.isInt() && to.isInt()) {
                        random(Coerce.toInteger(from), Coerce.toInteger(to))
                    } else {
                        random(Coerce.toDouble(from), Coerce.toDouble(to))
                    }
                }.join()
            }
        } else {
            val future = CompletableFuture<Any?>()
            frame.newFrame(action).run<Any?>().thenAcceptAsync({
                when (it) {
                    is Collection<*> -> random(future, it.map { i -> i as Any }.toList())
                    is Array<*> -> random(future, it.map { i -> i as Any }.toList())
                    else -> random(future, listOf(it))
                }
            }, frame.context().executor)
            return future
        }
    }

    fun random(future: CompletableFuture<Any?>, i: List<Any>) {
        future.complete(if (i.isEmpty()) null else i[Random.nextInt(i.size)])
    }

    @Suppress("UNCHECKED_CAST")
    internal object Parser {

        /**
         * random 1 to 10
         * random players
         * random range 1 to 10
         */
        @KetherParser(["random", "random2"])
        fun parser() = scriptParser {
            it.mark()
            try {
                var from = ParsedAction(LiteralAction<Any>("0.0"))
                var to = it.nextParsedAction()
                it.mark()
                try {
                    it.expect("to")
                    val next = it.nextParsedAction()
                    from = to as ParsedAction<Any>
                    to = next
                } catch (ignored: Exception) {
                    it.reset()
                }
                ActionRandom(from, to)
            } catch (ignored: Exception) {
                it.reset()
                ActionRandom(ParsedAction(LiteralAction<Any>("0.0")), ParsedAction(LiteralAction<Any>("0.0")), it.nextParsedAction())
            }
        }
    }
}