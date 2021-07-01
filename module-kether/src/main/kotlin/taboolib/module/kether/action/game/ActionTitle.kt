package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import org.bukkit.entity.Player
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.action.supplier.ActionPass
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTitle(val title: ParsedAction<*>, val subTitle: ParsedAction<*>, val fadeIn: Int, val stay: Int, val fadeOut: Int) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        return frame.newFrame(title).run<Any>().thenAccept { t ->
            frame.newFrame(subTitle).run<Any>().thenAccept { s ->
                val viewer = frame.script().sender as? Player ?: error("No player selected.")
                val title = t.toString().trimIndent().replace("@sender", viewer.name)
                val subTitle = s.toString().trimIndent().replace("@sender", viewer.name)
                viewer.sendTitle(title, subTitle, fadeIn, stay, fadeOut)
            }
        }
    }

    override fun toString(): String {
        return "ActionTitle(title=$title, subTitle=$subTitle, fadeIn=$fadeIn, stay=$stay, fadeOut=$fadeOut)"
    }

    companion object {

        @KetherParser(["title"])
        fun parser() = scriptParser {
            val title = it.next(ArgTypes.ACTION)
            it.mark()
            val subTitle = try {
                it.expect("subtitle")
                it.next(ArgTypes.ACTION)
            } catch (ignored: Exception) {
                it.reset()
                ParsedAction(ActionPass())
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