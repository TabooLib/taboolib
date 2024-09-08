package taboolib.module.lang.gameside

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.replaceWithOrder
import taboolib.module.lang.Type

/**
 * TabooLib
 * taboolib.module.lang.gameside.TypeTitle
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
        fadein = source["fadein"].toString().toIntOrNull() ?: 0
        stay = source["stay"].toString().toIntOrNull() ?: 20
        fadeout = source["fadeout"].toString().toIntOrNull() ?: 0
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (sender is ProxyPlayer) {
            val title = title?.translate(sender, *args)?.replaceWithOrder(*args) ?: ""
            val subtitle = subtitle?.translate(sender, *args)?.replaceWithOrder(*args) ?: ""
//            // 暂不支持
//            if (Language.enableSimpleComponent) {
//                // 仅限于 Bukkit
//                if (runningPlatform == Platform.BUKKIT) {
//                    try {
//                        val rawTitle = title.component().build().toRawMessage()
//                        val rawSubtitle = subtitle.component().build().toRawMessage()
//                        nmsProxy<NMSMessage>().sendRawTitle(sender.cast(), rawTitle, rawSubtitle, fadein, stay, fadeout)
//                    } catch (ex: NoClassDefFoundError) {
//                        warning("RawTitle requires module-nms")
//                    }
//                } else {
//                    warning("RawTitle is not supported on ${runningPlatform}.")
//                    sender.sendTitle(title, subtitle, fadein, stay, fadeout)
//                }
//            } else {
//                sender.sendTitle(title, subtitle, fadein, stay, fadeout)
//            }
            sender.sendTitle(title, subtitle, fadein, stay, fadeout)
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "NodeTitle(title=$title, subtitle=$subtitle, fadein=$fadein, stay=$stay, fadeout=$fadeout)"
    }
}