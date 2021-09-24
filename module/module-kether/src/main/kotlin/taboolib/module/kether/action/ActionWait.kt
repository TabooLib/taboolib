package taboolib.module.kether.action

import taboolib.common.platform.function.submit
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionWait(val ticks: Long) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val bukkitTask = submit(delay = ticks) {
            future.complete(null)
        }
        frame.addClosable(AutoCloseable {
            bukkitTask.cancel()
        })
        return future
    }

    internal object Parser {

        @KetherParser(["wait", "delay", "sleep"])
        fun parser() = scriptParser {
            ActionWait(it.next(ArgTypes.DURATION).toMillis() / 50L)
        }
    }
}