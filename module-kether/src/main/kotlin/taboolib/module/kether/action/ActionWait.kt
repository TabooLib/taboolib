package taboolib.module.kether.action

import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common.platform.submit
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionWait(val ticks: Long) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val bukkitTask = submit(delay = ticks) {
            future.complete(null)
        }
        frame.addClosable(AutoCloseable {
            bukkitTask.cancel()
        })
        return future
    }

    override fun toString(): String {
        return "ActionWait(tick=$ticks)"
    }

    companion object {

        @KetherParser(["wait", "delay", "sleep"])
        fun parser() = ScriptParser.parser {
            ActionWait(it.next(ArgTypes.DURATION).toMillis() / 50L)
        }
    }
}