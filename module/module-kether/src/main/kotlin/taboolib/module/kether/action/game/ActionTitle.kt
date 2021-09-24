package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.actions.LiteralAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTitle(val title: ParsedAction<*>, val subTitle: ParsedAction<*>, val fadeIn: Int, val stay: Int, val fadeOut: Int) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        return frame.newFrame(title).run<Any>().thenAccept { t ->
            frame.newFrame(subTitle).run<Any>().thenAccept { s ->
                val viewer = frame.script().sender as? ProxyPlayer ?: error("No player selected.")
                val title = t.toString().trimIndent().replace("@sender", viewer.name)
                val subTitle = s.toString().trimIndent().replace("@sender", viewer.name)
                viewer.sendTitle(title, subTitle, fadeIn, stay, fadeOut)
            }
        }
    }

    internal object Parser {

        @KetherParser(["title"])
        fun parser() = scriptParser {
            val title = it.next(ArgTypes.ACTION)
            it.mark()
            val subTitle = try {
                it.expect("subtitle")
                it.next(ArgTypes.ACTION)
            } catch (ignored: Exception) {
                it.reset()
                ParsedAction(LiteralAction<String>(""))
            }
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
            ActionTitle(title, subTitle, fadeIn, stay, fadeOut)
        }
    }
}