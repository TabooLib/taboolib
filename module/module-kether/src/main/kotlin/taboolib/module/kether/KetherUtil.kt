package taboolib.module.kether

import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.library.kether.*
import taboolib.module.kether.action.ActionLiteral
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

typealias Script = Quest

typealias ScriptFrame = QuestContext.Frame

fun <T> runKether(func: () -> T): T? {
    try {
        return func()
    } catch (ex: Exception) {
        ex.printKetherErrorMessage()
    }
    return null
}

fun <T> scriptParser(resolve: (QuestReader) -> QuestAction<T>): ScriptActionParser<T> {
    return ScriptActionParser(resolve)
}

fun String.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return KetherScriptLoader().load(ScriptService, "temp_${UUID.randomUUID()}", toByteArray(StandardCharsets.UTF_8), namespace)
}

fun List<String>.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return joinToString("\n").parseKetherScript(namespace)
}

fun QuestContext.Frame.player(): ProxyPlayer {
    return script().sender as? ProxyPlayer ?: error("No player selected.")
}

fun QuestContext.Frame.script(): ScriptContext {
    return context() as ScriptContext
}

fun QuestContext.Frame.deepVars(): HashMap<String, Any?> {
    return HashMap<String, Any?>().also { map ->
        var parent = parent()
        while (parent.isPresent) {
            map.putAll(parent.get().variables().keys().map { it to variables().get<Any>(it).orElse(null) })
            parent = parent.get().parent()
        }
        map.putAll(variables().keys().map { it to variables().get<Any>(it).orElse(null) })
    }
}

fun Throwable.printKetherErrorMessage() {
    if (javaClass.name.endsWith("kether.LocalizedException")) {
        warning("Unexpected exception while parsing kether script:")
        localizedMessage.split('\n').forEach { warning(it) }
    } else {
        printStackTrace()
    }
}

fun Any?.inferType(): Any? {
    if (this !is String) return this
    toIntOrNull()?.let { return it }
    toLongOrNull()?.let { return it }
    toDoubleOrNull()?.let { return it }
    toBooleanStrictOrNull()?.let { return it }
    return this
}

fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run { map.forEach { (k, v) -> set(k, v) } }
}

fun QuestReader.expects(vararg args: String): String {
    val element = nextToken()
    if (element !in args) {
        throw LoadError.NOT_MATCH.create("[${args.joinToString(", ")}]", element)
    }
    return element
}

fun QuestReader.switch(func: ExpectDSL.() -> Unit): ScriptAction<*> {
    val ed = ExpectDSL()
    func(ed)
    return try {
        mark()
        val sel = expects(*ed.method.keys.toTypedArray())
        ed.method[sel]!!()
    } catch (ex: Exception) {
        reset()
        if (ed.other == null) {
            throw ex
        }
        ed.other?.invoke(this) ?: throw ex
    }
}

fun actionNow(name: String = "kether-action-dsl", func: QuestContext.Frame.() -> Any?): ScriptAction<Any?> {
    return object : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return CompletableFuture.completedFuture(func(frame))
        }

        override fun toString(): String {
            return "KetherDSL($name)"
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun actionTake(name: String = "kether-action-dsl", func: QuestContext.Frame.() -> CompletableFuture<*>): ScriptAction<Any?> {
    return object : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return func(frame) as CompletableFuture<Any?>
        }

        override fun toString(): String {
            return "KetherDSL($name)"
        }
    }
}

fun actionFuture(name: String = "kether-action-dsl", func: QuestContext.Frame.(CompletableFuture<Any?>) -> Any?): ScriptAction<Any?> {
    return object : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()
            func(frame, future)
            return future
        }

        override fun toString(): String {
            return "KetherDSL($name)"
        }
    }
}

class ExpectDSL {

    val method = HashMap<String, QuestReader.() -> ScriptAction<*>>()
    var other: (QuestReader.() -> ScriptAction<*>?)? = null
        private set

    fun case(vararg str: String, func: QuestReader.() -> ScriptAction<*>) {
        str.forEach { method[it] = func }
    }

    fun other(func: QuestReader.() -> ScriptAction<*>?) {
        other = func
    }
}

inline fun <T> Iterable<T>.subBy(selector: (T) -> Int): Int {
    var sum = selector(firstOrNull() ?: return 0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum -= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.subByDouble(selector: (T) -> Double): Double {
    var sum = selector(firstOrNull() ?: return 0.0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum -= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.mulBy(selector: (T) -> Int): Int {
    var sum = 1
    for (element in this) {
        sum *= selector(element)
    }
    return sum
}

inline fun <T> Iterable<T>.mulByDouble(selector: (T) -> Double): Double {
    var sum = 1.0
    for (element in this) {
        sum *= selector(element)
    }
    return sum
}

inline fun <T> Iterable<T>.divBy(selector: (T) -> Int): Int {
    var sum = selector(firstOrNull() ?: return 0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum /= selector(element)
        }
    }
    return sum
}

inline fun <T> Iterable<T>.divByDouble(selector: (T) -> Double): Double {
    var sum = selector(firstOrNull() ?: return 0.0)
    forEachIndexed { index, element ->
        if (index > 0) {
            sum /= selector(element)
        }
    }
    return sum
}

fun Any.isInt(): Boolean {
    return try {
        Integer.parseInt(toString())
        true
    } catch (ex: Exception) {
        false
    }
}

fun literalAction(any: Any): ParsedAction<*> {
    return ParsedAction(ActionLiteral<Any>(any))
}

fun QuestContext.Frame.run(action: ParsedAction<*>): CompletableFuture<Any?> {
    return newFrame(action).run()
}

fun <T> CompletableFuture<Any?>.str(then: (String) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent() ?: "") }
}

fun <T> CompletableFuture<Any?>.strOrNull(then: (String?) -> T): CompletableFuture<T> {
    return thenApply { then(it?.toString()?.trimIndent()) }
}

fun <T> CompletableFuture<Any?>.bool(then: (Boolean) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toBoolean(it)) }
}

fun <T> CompletableFuture<Any?>.int(then: (Int) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toInteger(it)) }
}

fun <T> CompletableFuture<Any?>.long(then: (Long) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toLong(it)) }
}

fun <T> CompletableFuture<Any?>.double(then: (Double) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toDouble(it)) }
}

fun <T> CompletableFuture<Any?>.float(then: (Float) -> T): CompletableFuture<T> {
    return thenApply { then(Coerce.toFloat(it)) }
}