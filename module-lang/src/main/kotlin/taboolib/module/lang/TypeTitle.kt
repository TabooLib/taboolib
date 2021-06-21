package taboolib.module.lang

import io.izzel.kether.common.util.Coerce
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.replaceWithOrder

/**
 * TabooLib
 * taboolib.module.lang.TypeTitle
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeTitle : Type {

    var title: String? = null
    var subtitle: String? = null
    var fadein = 0
    var stay = 20
    var fadeout = 0

    override fun init(source: Map<String, Any>) {
        title = source["title"].toString()
        subtitle = source["subtitle"].toString()
        fadein = Coerce.toInteger(source["fadein"])
        stay = Coerce.toInteger(source["stay"])
        fadeout = Coerce.toInteger(source["fadeout"])
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (sender is ProxyPlayer) {
            sender.sendTitle(title?.replaceWithOrder(*args)?.translate(sender), subtitle?.replaceWithOrder(*args)?.translate(sender), fadein, stay, fadeout)
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "NodeTitle(title=$title, subtitle=$subtitle, fadein=$fadein, stay=$stay, fadeout=$fadeout)"
    }
}