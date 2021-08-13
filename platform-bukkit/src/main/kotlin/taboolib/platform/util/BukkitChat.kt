@file:Isolated

package taboolib.platform.util

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Isolated
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.SubscribeEvent
import taboolib.common.platform.submit
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import java.util.concurrent.ConcurrentHashMap

fun Player.nextChat(function: (message: String) -> Unit) {
    ChatListener.inputs[name] = function
}


fun Player.nextChat(function: (message: String) -> Unit, onReuse: (player: Player) -> Unit = {}) {
    if (!ChatListener.inputs.containsKey(name)) {
        ChatListener.inputs[name] = function
    } else onReuse(this)
}


fun Player.nextChatInTick(
    tick: Long,
    function: (message: String) -> Unit,
    onTimeOver: (player: Player) -> Unit = {},
    onReuse: (player: Player) -> Unit = {},
) {
    if (!ChatListener.inputs.containsKey(name)) {
        ChatListener.inputs[name] = function
        Bukkit.getScheduler().runTaskLater(
            BukkitAdapter().plugin,
            Runnable {
                if (ChatListener.inputs.containsKey(name)) {
                    onTimeOver(this)
                    ChatListener.inputs.remove(name)
                }
            }, tick
        )
    } else onReuse(this)
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
