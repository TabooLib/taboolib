package taboolib.module.kether

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.QuestReader
import io.izzel.kether.common.util.LocalizedException
import taboolib.common.platform.warning
import taboolib.common5.util.Coerce
import taboolib.module.kether.Kether.expects
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

fun QuestContext.Frame.script() = context() as ScriptContext

fun QuestContext.Frame.deepVars() = HashMap<String, Any?>().also { map ->
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().keys().map { it to variables().get<Any>(it).orElse(null) })
        parent = parent.get().parent()
    }
    map.putAll(variables().keys().map { it to variables().get<Any>(it).orElse(null) })
}

fun Throwable.printMessage() {
    if (this is LocalizedException) {
        warning("Unexpected exception while parsing kether script:")
        localizedMessage.split("\n").forEach { warning(it) }
    } else {
        printStackTrace()
    }
}

fun Any?.inferType(): Any? {
    val asInteger = asInteger(this)
    if (asInteger.isPresent) {
        return asInteger.get()
    }
    val asLong = Coerce.asLong(this)
    if (asLong.isPresent) {
        return asLong.get()
    }
    val asDouble = Coerce.asDouble(this)
    if (asDouble.isPresent) {
        return asDouble.get()
    }
    val asBoolean = Coerce.asBoolean(this)
    if (asBoolean.isPresent) {
        return asBoolean.get()
    }
    return this
}

private fun asInteger(obj: Any?) = when (obj) {
    null -> {
        Optional.empty()
    }
    is Number -> {
        Optional.of(obj.toInt())
    }
    else -> {
        try {
            Optional.ofNullable(Integer.valueOf(obj.toString()))
        } catch (ignored: NumberFormatException) {
            Optional.empty()
        }
    }
}

fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run {
        map.forEach { (k, v) -> set(k, v) }
    }
}

fun QuestReader.switch(func: ExpectDSL.() -> Unit): QuestAction<*> {
    val ed = ExpectDSL()
    func(ed)
    return try {
        val sel = expects(*ed.method.keys.toTypedArray())
        ed.method[sel]!!()
    } catch (ex: Exception) {
        if (ed.other == null) {
            throw ex
        }
        ed.other?.invoke(this) ?: throw ex
    }
}

fun actionNow(name: String = "kether-action-del", func: QuestContext.Frame.() -> Any?): QuestAction<*> {
    return object : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
            return CompletableFuture.completedFuture(func(frame))
        }

        override fun toString(): String {
            return "KetherDSL($name)"
        }
    }
}

fun actionFuture(name: String = "kether-action-del", func: QuestContext.Frame.(CompletableFuture<Any?>) -> Any?): QuestAction<*> {
    return object : QuestAction<Any?>() {

        override fun process(frame: QuestContext.Frame): CompletableFuture<Any?> {
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

    val method = HashMap<String, QuestReader.() -> QuestAction<*>>()
    var other: (QuestReader.() -> QuestAction<*>?)? = null
        private set

    fun case(vararg str: String, func: QuestReader.() -> QuestAction<*>) {
        str.forEach {
            method[it] = func
        }
    }

    fun other(func: QuestReader.() -> QuestAction<*>?) {
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
        Integer.parseInt(this.toString())
        true
    } catch (ex: java.lang.Exception) {
        false
    }
}