package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import taboolib.common.platform.ProxyEvent
import taboolib.common.platform.server
import taboolib.common5.util.compileJS
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import taboolib.module.kether.deepVars
import java.util.concurrent.CompletableFuture
import javax.script.CompiledScript
import javax.script.SimpleBindings

/**
 * @author IzzelAliz
 */
class ActionJavaScript(val script: CompiledScript) : QuestAction<Any>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Any> {
        val s = (context.context() as ScriptContext)
        val r = try {
            val bindings = hashMapOf("event" to s.event, "sender" to s.sender, "server" to server())
            bindings.putAll(context.deepVars())
            script.eval(SimpleBindings(Event(bindings, s).bindings))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return CompletableFuture.completedFuture(r)
    }

    override fun toString(): String {
        return "ActionJavaScript(script=$script)"
    }

    class Event(val bindings: MutableMap<String, Any?>, val context: ScriptContext) : ProxyEvent() {

        override val allowAsynchronous: Boolean
            get() = true

        override val allowCancelled: Boolean
            get() = false
    }

    companion object {

        @KetherParser(["$", "js", "javascript"])
        fun parser() = ScriptParser.parser {
            ActionJavaScript(it.nextToken().trimIndent().compileJS()!!)
        }
    }
}