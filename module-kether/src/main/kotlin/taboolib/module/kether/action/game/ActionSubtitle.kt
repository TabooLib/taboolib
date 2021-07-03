package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.common.platform.ProxyPlayer
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSubtitle(val subTitle: ParsedAction<*>, val fadeIn: Int, val stay: Int, val fadeOut: Int) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        return frame.newFrame(subTitle).run<Any>().thenAccept { s ->
            val viewer = frame.script().sender as? ProxyPlayer ?: error("No player selected.")
            val subTitle = s.toString().trimIndent().replace("@sender", viewer.name)
            viewer.sendTitle("", subTitle, fadeIn, stay, fadeOut)
        }
    }

    internal object Parser {

        @KetherParser(["subtitle"])
        fun parser() = scriptParser {
            val subTitle = it.next(ArgTypes.ACTION)
            var fadeIn = 0
            var stay = 20
            var fadeOut = 0
            it.mark()
            try {
                it.expects("by", "with")
                fadeIn = it.nextInt()
                stay = it.nextInt()
                fadeOut = it.nextInt()
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionSubtitle(subTitle, fadeIn, stay, fadeOut)
        }
    }
}