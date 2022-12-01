package taboolib.module.kether

import taboolib.library.kether.Parser
import taboolib.library.kether.Parsers
import java.util.concurrent.CompletableFuture

object ParserDSL {

    fun string(): Parser<String> = Parsers.string()

    fun int(): Parser<Int> = Parsers.integer()

    fun double(): Parser<Double> = Parsers.decimal()

    fun float(): Parser<Float> = Parsers.decimal().map { it.toFloat() }

    fun <T> now(action: (ScriptFrame) -> T): Parser.Action<T> = Parser.Action { CompletableFuture.completedFuture(action(it)) }

    fun <T> future(action: (ScriptFrame) -> CompletableFuture<T>): Parser.Action<T> = Parser.Action { action(it) }

    fun <A, B> Parser<A>.and(b: Parser<B>): Parser<Pair<A, B>> = Parsers.and(this, b)

    fun <A> Parser<A>.option(): Parser<A?> = TODO()
}