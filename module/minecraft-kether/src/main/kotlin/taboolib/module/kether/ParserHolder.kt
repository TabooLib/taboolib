package taboolib.module.kether

import taboolib.common5.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Parser
import taboolib.library.kether.Parser.Action
import java.util.concurrent.CompletableFuture

object ParserHolder {

    /** 取任意类型 */
    fun any(): Parser<Any?> {
        return Parser.frame { r ->
            val action = r.nextParsedAction()
            Action { it.run(action) }
        }
    }

    /** 取任意类型并转换为列表 */
    @Suppress("UNCHECKED_CAST")
    fun anyAsList(): Parser<MutableList<Any?>> {
        return Parser.frame { r ->
            val action = r.nextParsedAction()
            Action { it.run(action).thenApply { obj -> if (obj is MutableList<*>) obj as MutableList<Any?> else mutableListOf(obj) } }
        }
    }

    /**
     * 取原始列表
     * 例如：[1, 2, 3]
     */
    fun originList(): Parser<MutableList<Any?>> = any().listOf()

    /** 取特定类型 */
    inline fun <reified T> type(): Parser<T> = any().map { it as T }

    /** 取动作 */
    fun action(): Parser<ParsedAction<*>> = Parser.of { it.nextParsedAction() }

    /** 取动作列表 */
    fun actionList(): Parser<MutableList<ParsedAction<*>>> = Parser.of { it.next(ArgTypes.listOf(ArgTypes.ACTION)) }

    /** 取文本（固定的）*/
    fun symbol(): Parser<String> = Parser.of { it.nextToken() }

    /** 取文本 */
    fun text(): Parser<String> = any().map(Coerce::toString).orElse(symbol())

    /** 取整数 */
    fun int(): Parser<Int> = Parser.of { it.nextInt() }.orElse(any().map { it.cint })

    /** 取长整数 */
    fun long(): Parser<Long> = Parser.of { it.nextLong() }.orElse(any().map { it.clong })

    /** 取双精度浮点数 */
    fun double(): Parser<Double> = Parser.of { it.nextDouble() }.orElse(any().map { it.cdouble })

    /** 取浮点数 */
    fun float(): Parser<Float> = double().map { it.toFloat() }

    /** 取布尔值 */
    fun bool(): Parser<Boolean> = Parser.of { Coerce.asBoolean(it.nextToken()).get() }.orElse(any().map { it.cbool })

    /** 运行并返回结果 */
    fun <T> now(action: ScriptFrame.() -> T): Action<T> {
        return Action { CompletableFuture.completedFuture(action(it)) }
    }

    /** 运行并返回回调函数 */
    fun <T> future(action: ScriptFrame.() -> CompletableFuture<T>): Action<T> {
        return Action { action(it) }
    }

    /** 二元并列参数 */
    fun <A, B> Parser<A>.and(b: Parser<B>): Parser<Pair<A, B>> {
        return fold(b) { fa, fb -> Pair(fa, fb) }
    }

    /** 三元并列参数 */
    fun <A, B, C> Parser<A>.and(b: Parser<B>, c: Parser<C>): Parser<Triple<A, B, C>> {
        return fold(b, c) { fa, fb, fc -> Triple(fa, fb, fc) }
    }

    /** 为可选 */
    fun <A> Parser<A>.option(): Parser<A?> {
        return optional().map { it.orElse(null) }
    }

    /** 默认值 */
    fun <A> Parser<A?>.defaultsTo(value: A): Parser<A> {
        return map { it ?: value }
    }

    /** 子语句 */
    fun <A> command(vararg s: String, then: Parser<A>): Parser<A> {
        return Parser.frame { r ->
            r.expects(*s)
            then.reader.apply(r)
        }
    }

    /** 快速构建完成的 [CompletableFuture] */
    fun <T> completedFuture(value: T): CompletableFuture<T> {
        return CompletableFuture.completedFuture(value)
    }
}