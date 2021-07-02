package taboolib.module.kether.action.transform

import taboolib.common.platform.ProxyEvent
import taboolib.common.platform.server
import taboolib.common5.util.compileJS
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture
import javax.script.CompiledScript
import javax.script.SimpleBindings

/**
 * @author IzzelAliz
 */
class ActionJavaScript(val script: CompiledScript) : ScriptAction<Any>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        val s = frame.script()
        val r = try {
            val bindings = hashMapOf("event" to s.event, "sender" to s.sender, "server" to server())
            bindings.putAll(frame.deepVars())
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
        fun parser() = scriptParser {
            ActionJavaScript(it.nextToken().trimIndent().compileJS()!!)
        }
    }
}