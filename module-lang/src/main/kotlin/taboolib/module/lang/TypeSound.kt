package taboolib.module.lang

import io.izzel.kether.common.util.Coerce
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
        volume = Coerce.toFloat(source["volume"] ?: source["v"])
        pitch = Coerce.toFloat(source["pitch"] ?: source["p"])
        resource = Coerce.toBoolean(source["resource"])
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (sender is ProxyPlayer) {
            if (resource) {
                sender.playSoundResource(sender.location, sound, volume, pitch)
            } else {
                sender.playSound(sender.location, sound, volume, pitch)
            }
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "NodeSound(sound='$sound', volume=$volume, pitch=$pitch)"
    }
}