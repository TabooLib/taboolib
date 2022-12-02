package taboolib.module.kether

import taboolib.common5.*
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Parser
import taboolib.library.kether.Parser.Action
import java.util.concurrent.CompletableFuture

object ParserHolder {

    fun any(): Parser<Any> {
        return Parser.frame { r ->
            val action = r.nextParsedAction()
            Action { it.run(action) }
        }
    }

    inline fun <reified T> type(): Parser<T> = any().map { it as T }

    fun action(): Parser<ParsedAction<*>> = Parser.of { it.nextParsedAction() }

    fun symbol(): Parser<String> = Parser.of { it.nextToken() }

    fun text(): Parser<String> = any().map(Coerce::toString).orElse(symbol())

    fun int(): Parser<Int> = Parser.of { it.nextInt() }.orElse(any().map { it.cint })

    fun double(): Parser<Double> = Parser.of { it.nextDouble() }.orElse(any().map { it.cdouble })

    fun float(): Parser<Float> = double().map { it.toFloat() }

    fun bool(): Parser<Boolean> = Parser.of { Coerce.asBoolean(it.nextToken()).get() }.orElse(any().map { it.cbool })

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

    fun <A> Parser<A?>.defaultsTo(value: A): Parser<A> = this.map { it ?: value }

    fun <A> command(vararg s: String, then: Parser<A>): Parser<A> {
        return Parser.frame { r ->
            r.expects(*s)
            then.reader.apply(r)
        }
    }
}