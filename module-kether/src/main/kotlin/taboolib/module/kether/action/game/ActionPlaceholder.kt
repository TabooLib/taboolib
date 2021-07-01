package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import me.clip.placeholderapi.PlaceholderAPI
import taboolib.common.reflect.Reflex.Companion.staticInvoke
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPlaceholder(val source: ParsedAction<*>) : ScriptAction<String>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<String> {
        return frame.newFrame(source).run<Any>().thenApplyAsync({
            PlaceholderAPI::class.java.staticInvoke<String>("setPlaceholders", frame.script().sender ?: error("No event selected."), it.toString().trimIndent())
        }, frame.context().executor)
    }

    override fun toString(): String {
        return "ActionPlaceholder(source='$source')"
    }

    companion object {

        @KetherParser(["papi", "placeholder"])
        fun parser() = scriptParser {
            ActionPlaceholder(it.next(ArgTypes.ACTION))
        }
    }
}