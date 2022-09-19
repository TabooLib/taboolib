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
        val future = CompletableFuture<Any?>()
        if (action == null) {
            frame.run(from).str { from ->
                frame.run(to).str { to ->
                    if (from.isInt() && to.isInt()) {
                        future.complete(random(Coerce.toInteger(from), Coerce.toInteger(to)))
                    } else {
                        future.complete(random(Coerce.toDouble(from), Coerce.toDouble(to)))
                    }
                }
            }
        } else {
            frame.run(action).thenAccept {
                when (it) {
                    is Collection<*> -> random(future, it.map { i -> i as Any })
                    is Array<*> -> random(future, it.map { i -> i as Any })
                    else -> {
                        future.complete(if (it?.isInt() == true) random(Coerce.toInteger(it)) else random(0.0, Coerce.toDouble(it)))
                    }
                }
            }
        }
        return future
    }

    fun random(future: CompletableFuture<Any?>, i: List<Any>) {
        future.complete(if (i.isEmpty()) null else i[Random.nextInt(i.size)])
    }

    object Parser {

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