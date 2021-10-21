package taboolib.module.kether.action.transform

import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
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
class ActionRandom(val from: Double, val to: Double, val action: ParsedAction<*>? = null) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        if (action == null) {
            return CompletableFuture.completedFuture(taboolib.common.util.random(from, to))
        } else {
            val future = CompletableFuture<Any?>()
            frame.newFrame(action).run<Any?>().thenAcceptAsync( {
                when (it) {
                    is Collection<*> -> {
                        random(future, it.map { i -> i as Any }.toList())
                    }
                    is Array<*> -> {
                        random(future, it.map { i -> i as Any }.toList())
                    }
                    else -> {
                        random(future, listOf(it))
                    }
                }
            }, frame.context().executor)
            return future
        }
    }

    fun random(future: CompletableFuture<Any?>, i: List<Any>) {
        future.complete(if (i.isEmpty()) null else i[Random.nextInt(i.size)])
    }

    internal object Parser {

        /**
         * random 1 to 10
         * random players
         * random range 1 to 10
         */
        @KetherParser(["random"])
        fun parser() = scriptParser {
            it.mark()
            try {
                var from = 0.0
                var to = it.nextDouble()
                it.mark()
                try {
                    it.expect("to")
                    it.nextDouble().run {
                        from = to
                        to = this
                    }
                } catch (ignored: Exception) {
                    it.reset()
                }
                ActionRandom(from, to)
            } catch (ignored: Exception) {
                it.reset()
                ActionRandom(0.0, 0.0, it.next(ArgTypes.ACTION))
            }
        }

        @KetherParser(["random2"])
        fun random2() = scriptParser {
            val from = it.next(ArgTypes.ACTION)
            it.expect("to")
            val to = it.next(ArgTypes.ACTION)
            actionFuture {
                newFrame(from).run<Any>().thenAccept { from ->
                    newFrame(to).run<Any>().thenAccept { to ->
                        if (from.isInt() && to.isInt()) {
                            it.complete(random(Coerce.toInteger(from), Coerce.toInteger(to)))
                        } else {
                            it.complete(random(Coerce.toDouble(from), Coerce.toDouble(to)))
                        }
                    }
                }
            }
        }
    }
}