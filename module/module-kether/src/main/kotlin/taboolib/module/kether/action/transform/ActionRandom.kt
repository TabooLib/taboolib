package taboolib.module.kether.action.transform

import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.library.kether.ParsedAction
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
                    is Collection<*> -> {
                        random(future, it.map { i -> i as Any }.toList())
                    }
                    is Array<*> -> {
                        random(future, it.map { i -> i as Any }.toList())
                    }
                    else -> {
                        future.complete(if (it.isInt()) random(Coerce.toInteger(it)) else random(0.0, Coerce.toDouble(it)))
                    }
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
            val from = it.nextParsedAction()
            try {
                it.mark()
                it.expect("to")
                val to = it.nextParsedAction()
                ActionRandom(from, to)
            } catch (_: Exception) {
                it.reset()
                ActionRandom(literalAction(0), literalAction(0), from)
            }
        }
    }
}