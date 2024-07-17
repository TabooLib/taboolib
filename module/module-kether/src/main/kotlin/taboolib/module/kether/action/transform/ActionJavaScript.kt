package taboolib.module.kether.action.transform

import taboolib.common.Inject
import taboolib.common.platform.function.server
import taboolib.common5.compileJS
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture
import javax.script.CompiledScript
import javax.script.SimpleBindings

/**
 * @author IzzelAliz
 */
internal class ActionJavaScript(val script: CompiledScript) : ScriptAction<Any>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
        val s = frame.script()
        val r = try {
            val bindings: MutableMap<String, Any?> = hashMapOf("sender" to s.sender, "server" to server())
            bindings.putAll(frame.deepVars())
            script.eval(SimpleBindings(bindings))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return CompletableFuture.completedFuture(r)
    }

    @Inject
    internal companion object {

        @KetherParser(["$", "js", "javascript"])
        fun parser() = scriptParser {
            ActionJavaScript(it.nextToken().trimIndent().compileJS()!!)
        }
    }
}