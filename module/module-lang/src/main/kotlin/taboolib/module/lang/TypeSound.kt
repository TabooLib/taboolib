package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

/**
 * TabooLib
 * taboolib.module.lang.TypeSound
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeSound : Type {

    lateinit var sound: String
    var volume = 1f
    var pitch = 1f
    var resource = false

    override fun init(source: Map<String, Any>) {
        sound = source["sound"].toString()
        volume = (source["volume"] ?: source["v"]).toString().toFloatOrNull() ?: 1f
        pitch = (source["pitch"] ?: source["p"]).toString().toFloatOrNull() ?: 1f
        resource = source["resource"].toString().trim().matches("^(1|true|yes)\$".toRegex())
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (sender is ProxyPlayer) {
            if (resource) {
                sender.playSoundResource(sender.location, sound, volume, pitch)
            } else {
                try {
                    sender.playSound(sender.location, sound, volume, pitch)
                } catch (ignored: IllegalArgumentException) {
                }
            }
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "NodeSound(sound='$sound', volume=$volume, pitch=$pitch)"
    }
}