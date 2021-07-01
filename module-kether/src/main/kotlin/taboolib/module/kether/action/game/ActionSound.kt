package taboolib.module.kether.action.game

import io.izzel.kether.common.api.QuestContext
import org.bukkit.entity.Player
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSound(val sound: String, val volume: Float, val pitch: Float) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        val viewer = frame.script().sender as? Player ?: error("No player selected.")
        if (sound.startsWith("resource:")) {
            viewer.playSound(viewer.location, sound.substring("resource:".length), volume, pitch)
        } else {
            viewer.playSound(viewer.location, sound, volume, pitch)
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun toString(): String {
        return "ActionSound(sound='$sound', volume=$volume, pitch=$pitch)"
    }

    companion object {

        /**
         * sound block_stone_break by 1 1
         */
        @KetherParser(["sound"])
        fun parser() = scriptParser {
            val sound = it.nextToken()
            var volume = 1.0f
            var pitch = 1.0f
            it.mark()
            try {
                it.expects("by", "with")
                volume = it.nextDouble().toFloat()
                pitch = it.nextDouble().toFloat()
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionSound(sound, volume, pitch)
        }
    }
}