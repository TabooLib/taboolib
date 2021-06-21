package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender

/**
 * node:
 * - type: text
 *   text: hello world!
 * - type: title
 *   title: hello world!
 *   subtitle: sub
 *   fadein: 1
 *   stay: 1
 *   fadeout: 1
 * - type: sound
 *   sound: block_stone_break
 *   volume: 1
 *   pitch: 1
 * - type: json
 *   text:
 *   - [hello] [world!]
 *   args:
 *   - hover: hello
 *     command: say hello
 *   - hover: world!
 *     command: say world
 *
 * TabooLib
 * taboolib.module.lang.Type
 *
 * @author sky
 * @since 2021/6/20 10:53 下午
 */
interface Type {

    fun init(source: Map<String, Any>)

    fun send(sender: ProxyCommandSender, vararg args: Any)

    fun String.translate(sender: ProxyCommandSender): String {
        var s = this
        Language.textTransfer.forEach { s = it.translate(sender, s) }
        return s
    }
}