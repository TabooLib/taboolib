@file:Isolated

package taboolib.module.chat

import net.md_5.bungee.api.ChatColor
import taboolib.common.Isolated

/**
 * 对字符串上色
 */
fun String.colored() = HexColor.translate(this)

/**
 * 对字符串去色
 */
fun String.uncolored() = ChatColor.stripColor(this.colored())!!

/**
 * 对列表上色
 */
fun List<String>.colored() = map { it.colored() }

/**
 * 对列表去色
 */
fun List<String>.uncolored() = map { it.uncolored() }