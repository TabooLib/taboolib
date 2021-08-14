package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
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