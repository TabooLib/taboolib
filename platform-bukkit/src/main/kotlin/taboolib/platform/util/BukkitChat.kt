@file:Isolated

package taboolib.platform.util

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Isolated
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import java.util.concurrent.ConcurrentHashMap

fun Player.nextChat(function: (message: String) -> Unit) {
    ChatListener.inputs[name] = function
}

fun Player.nextChat(function: (message: String) -> Unit, reuse: (player: Player) -> Unit = {}) {
    if (ChatListener.inputs.containsKey(name)) {
        reuse(this)
    } else {
        ChatListener.inputs[name] = function
    }
}

fun Player.nextChatInTick(tick: Long, func: (message: String) -> Unit, timeout: (player: Player) -> Unit = {}, reuse: (player: Player) -> Unit = {}) {
    if (ChatListener.inputs.containsKey(name)) {
        reuse(this)
    } else {
        ChatListener.inputs[name] = func
        submit(delay = tick) {
            if (ChatListener.inputs.containsKey(name)) {
                timeout(this@nextChatInTick)
                ChatListener.inputs.remove(name)
            }
        }
    }
}

@Isolated
@PlatformSide([Platform.BUKKIT])
internal object ChatListener {

    val inputs = ConcurrentHashMap<String, (String) -> Unit>()

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        inputs.remove(e.player.name)
    }

    @SubscribeEvent
    fun e(e: AsyncPlayerChatEvent) {
        if (inputs.containsKey(e.player.name)) {
            inputs.remove(e.player.name)?.invoke(e.message)
            e.isCancelled = true
        }
    }
}
