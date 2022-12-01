package taboolib.module.kether

import taboolib.common5.*
import taboolib.library.kether.Parser
import taboolib.library.kether.Parser.Action
import java.util.concurrent.CompletableFuture

object ParserHolder {

    inline fun <reified T> type(): Parser<T> {
        return Parser.frame { r ->
            val action = r.nextParsedAction()
            Action { it.run(action).thenApply { obj -> obj as T } }
        }
    }

    fun any(): Parser<Any> {
        return Parser.frame { r ->
            val action = r.nextParsedAction()
            Action { it.run(action) }
        }
    }

    fun text(): Parser<String> {
        return Parser.frame { r ->
            r.mark()
            try {
                val action = r.nextParsedAction()
                Action { f -> f.run(action).thenApply { obj -> Coerce.toString(obj) } }
            } catch (e: Exception) {
                r.reset()
                Action.point(r.nextToken())
            }
        }
    }

    fun int(): Parser<Int> {
        return Parser.frame { r ->
            r.mark()
            try {
                Action.point(r.nextInt())
            } catch (e: Exception) {
                r.reset()
                val action = r.nextParsedAction()
                Action { f -> f.run(action).thenApply { obj -> obj.cint } }
            }
        }
    }

    fun double(): Parser<Double> {
        return Parser.frame { r ->
            r.mark()
            try {
                Action.point(r.nextDouble())
            } catch (e: Exception) {
                r.reset()
                val action = r.nextParsedAction()
                Action { f -> f.run(action).thenApply { obj -> obj.cdouble } }
            }
        }
    }

    fun float(): Parser<Float> {
        return Parser.frame { r ->
            r.mark()
            try {
                Action.point(r.nextDouble().toFloat())
            } catch (e: Exception) {
                r.reset()
                val action = r.nextParsedAction()
                Action { f -> f.run(action).thenApply { obj -> obj.cfloat } }
            }
        }
    }

    fun bool(): Parser<Boolean> {
        return Parser.frame { r ->
            r.mark()
            try {
                Action.point(r.nextToken().cbool)
            } catch (e: Exception) {
                r.reset()
                val action = r.nextParsedAction()
                Action { f -> f.run(action).thenApply { obj -> obj.cbool } }
            }
        }
    }

    fun <T> now(action: ScriptFrame.() -> T): Action<T> {
        return Action { CompletableFuture.completedFuture(action(it)) }
    }

    fun <T> future(action: ScriptFrame.() -> CompletableFuture<T>): Action<T> {
        return Action { action(it) }
    }

    fun <A, B> Parser<A>.and(b: Parser<B>): Parser<Pair<A, B>> {
        return fold(b) { fa, fb -> Pair(fa, fb) }
    }

    fun <A, B, C> Parser<A>.and(b: Parser<B>, c: Parser<C>): Parser<Triple<A, B, C>> {
        return fold(b, c) { fa, fb, fc -> Triple(fa, fb, fc) }
    }

    fun <A> Parser<A>.option(): Parser<A?> {
        return optional().map { it.orElse(null) }
    }

    fun <A> command(vararg s: String, then: Parser<A>): Parser<A> {
        return Parser.frame { r ->
            r.expects(*s)
            then.reader.apply(r)
        }
    }
}