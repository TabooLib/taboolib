package taboolib.module.lang.gameside

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.replaceWithOrder
import taboolib.module.lang.Type

/**
 * TabooLib
 * taboolib.module.lang.gameside.TypeActionBar
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeActionBar : Type {

    lateinit var text: String

    override fun init(source: Map<String, Any>) {
        text = source["text"].toString()
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        val newText = text.translate(sender, *args).replaceWithOrder(*args)
        if (sender is ProxyPlayer) {
//            // 暂不支持
//            if (Language.enableSimpleComponent) {
//                // 仅限于 Bukkit
//                if (runningPlatform == Platform.BUKKIT) {
//                    try {
//                        nmsProxy<NMSMessage>().sendRawActionBar(sender.cast(), newText.component().build().toRawMessage())
//                    } catch (ex: NoClassDefFoundError) {
//                        warning("RawActionBar requires module-nms-util")
//                    }
//                } else {
//                    warning("RawActionBar is not supported on ${runningPlatform}.")
//                    sender.sendRawMessage(newText)
//                }
//            }
            sender.sendRawMessage(newText)
        } else {
            sender.sendMessage(newText)
        }
    }

    override fun toString(): String {
        return "NodeActionBar(text='$text')"
    }
}