package taboolib.module.chat

import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.module.chat.SimpleComponent
 *
 * @author 坏黑
 * @since 2023/2/9 21:49
 */
interface SimpleComponent {

    /** 构建为 RawMessage */
    fun build() = build { }

    /** 构建为 RawMessage */
    fun build(transfer: TextTransfer.() -> Unit): ComponentText

    /** 构建为 RawMessage，并上色 */
    fun buildColored() = build { colored() }

    /** 构建为 RawMessage，并上色 */
    fun buildColored(transfer: TextTransfer.() -> Unit): ComponentText {
        return build { colored() }
    }

    /** 直接构建为 RawMessage */
    fun buildToRaw() = buildToRaw { }

    /** 直接构建为 RawMessage */
    fun buildToRaw(transfer: TextTransfer.() -> Unit = {}): String {
        return buildColored(transfer).toRawMessage()
    }

    /** 广播给所有玩家 */
    fun broadcast() {
        build().broadcast()
    }

    /** 发送给玩家 */
    fun sendTo(sender: ProxyCommandSender) {
        build().sendTo(sender)
    }
}