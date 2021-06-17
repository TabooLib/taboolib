package taboolib.module.chat

fun String.colored() = HexColor.translate(this)

fun List<String>.colored() = map { HexColor.translate(it) }