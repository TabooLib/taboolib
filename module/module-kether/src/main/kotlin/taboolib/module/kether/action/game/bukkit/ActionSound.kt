package taboolib.module.kether.action.game.bukkit

import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSound(val sound: String, val volume: Float, val pitch: Float) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        val viewer = frame.script().sender?.castSafely<Player>() ?: error("No player selected.")
        if (sound.startsWith("resource:")) {
            viewer.playSound(viewer.location, sound.substring("resource:".length), volume, pitch)
        } else {
            kotlin.runCatching {
                viewer.playSound(viewer.location, Sound.valueOf(sound.replace('.', '_').uppercase()), volume, pitch)
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    internal object Parser {

        /**
         * sound block_stone_break by 1 1
         */
        @PlatformSide([Platform.BUKKIT])
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