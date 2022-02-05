@file:Isolated

package taboolib.module.chat

import net.md_5.bungee.api.ChatColor
import taboolib.common.Isolated

typealias TellrawJson = RawMessage

fun String.colored() = ColorTranslator.translate(this)

fun String.uncolored() = ChatColor.stripColor(this.colored())!!

fun List<String>.colored() = map { it.colored() }

fun List<String>.uncolored() = map { it.uncolored() }