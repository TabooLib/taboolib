package taboolib.module.kether.action.game.compat

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
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
            PlaceholderAPI.setPlaceholders(frame.script().sender?.castSafely<Player>() ?: error("No event selected."), it.toString().trimIndent())
        }, frame.context().executor)
    }

    internal object Parser {

        @KetherParser(["papi", "placeholder"])
        fun parser() = scriptParser {
            ActionPlaceholder(it.next(ArgTypes.ACTION))
        }
    }
}