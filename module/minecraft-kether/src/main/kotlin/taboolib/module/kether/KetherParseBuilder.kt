package taboolib.module.kether

import taboolib.library.kether.LoadError
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import taboolib.module.kether.action.ActionLiteral
import java.util.concurrent.CompletableFuture

/**
 * 创建一个 Literal 类型的 ParsedAction
 */
fun literalAction(any: Any): ParsedAction<*> {
    return ParsedAction(ActionLiteral<Any>(any))
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

fun actionNow(name: String = "actionNow", func: QuestContext.Frame.() -> Any?): ScriptAction<Any?> {
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
fun actionTake(name: String = "actionTake", func: QuestContext.Frame.() -> CompletableFuture<*>): ScriptAction<Any?> {
    return object : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return func(frame) as CompletableFuture<Any?>
        }

        override fun toString(): String {
            return "KetherDSL($name)"
        }
    }
}

fun actionFuture(name: String = "actionFuture", func: QuestContext.Frame.(CompletableFuture<Any?>) -> Any?): ScriptAction<Any?> {
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