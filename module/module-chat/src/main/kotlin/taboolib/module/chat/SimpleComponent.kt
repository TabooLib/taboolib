package taboolib.module.chat

/**
 * TabooLib
 * taboolib.module.chat.SimpleComponent
 *
 * @author 坏黑
 * @since 2023/2/9 21:49
 */
interface SimpleComponent {

    /** 构建为 RawMessage */
    fun build(transfer: TextTransfer.() -> Unit = {}): ComponentText
}