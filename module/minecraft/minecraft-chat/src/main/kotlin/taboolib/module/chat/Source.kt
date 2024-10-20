package taboolib.module.chat

import net.md_5.bungee.api.chat.BaseComponent
import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.module.chat.Source
 *
 * @author 坏黑
 * @since 2023/2/9 21:10
 */
interface Source {

    /** 转换为原始信息 */
    fun toRawMessage(): String

    /** 转换为带颜色的纯文本 */
    fun toLegacyText(): String

    /** 转换为纯文本 */
    fun toPlainText(): String

    /** 转换为 Spigot 对象 */
    fun toSpigotObject(): BaseComponent

    /** 转换为 RawMessage */
    fun toLegacyRawMessage(): RawMessage

    /** 广播给所有玩家 */
    fun broadcast()

    /** 发送给玩家 */
    fun sendTo(sender: ProxyCommandSender)
}