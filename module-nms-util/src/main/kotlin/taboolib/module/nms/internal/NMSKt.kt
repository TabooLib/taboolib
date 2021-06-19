package taboolib.module.nms.internal

import org.bukkit.entity.Player

abstract class NMSKt {

    val uniqueColors = listOf(
        "§黒",
        "§黓",
        "§黔",
        "§黕",
        "§黖",
        "§黗",
        "§默",
        "§黙",
        "§黚",
        "§黛",
        "§黜",
        "§黝",
        "§點",
        "§黟",
        "§黠",
        "§黡",
        "§黢",
        "§黣",
        "§黤",
        "§黥",
        "§黦",
        "§黧",
        "§黨",
        "§黩",
        "§黪",
        "§黫",
        "§黬",
        "§黭",
    )

    abstract fun setupScoreboard(player: Player, remove: Boolean)

    abstract fun setDisplayName(player: Player, title: String)

    abstract fun changeContent(player: Player, content: List<String>, lastContent: Map<Int, String>)

    abstract fun display(player: Player)
}