package taboolib.expansion

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.module.lang.asLangText

fun CommandBuilder.CommandComponent.createHelper() {
    execute<ProxyCommandSender> { sender, context, _ ->
        val command = context.command
        val builder = StringBuilder("§cUsage: /${command.name}")
        var newline = false
        fun space(space: Int): String {
            return (1..space).joinToString("") { " " }
        }
        fun print(compound: CommandBuilder.CommandComponent, index: Int, size: Int, offset: Int = 8, level: Int = 0, end: Boolean = false) {
            var commit = 0
            when (compound) {
                is CommandBuilder.CommandComponentLiteral -> {
                    if (size == 1) {
                        builder.append(" ").append("§c${compound.aliases[0]}")
                    } else {
                        newline = true
                        builder.appendLine()
                        builder.append(space(offset))
                        if (level > 1) {
                            builder.append(if (end) " " else "§7│")
                        }
                        builder.append(space(level))
                        if (index + 1 < size) {
                            builder.append("§7├── ")
                        } else {
                            builder.append("§7└── ")
                        }
                        builder.append("§c${compound.aliases[0]}")
                    }
                    commit = compound.aliases[0].length
                }
                is CommandBuilder.CommandComponentDynamic -> {
                    val value = if (compound.commit.startsWith("@")) {
                        sender.asLangText(compound.commit.substring(1))
                    } else {
                        compound.commit
                    }
                    commit = if (compound.optional) {
                        builder.append(" ").append("§7<$value§c?§7>")
                        compound.commit.length + 3
                    } else {
                        builder.append(" ").append("§7<$value>")
                        compound.commit.length + 2
                    }
                }
            }
            if (level > 0) {
                commit += 1
            }
            compound.children.forEachIndexed { i, children ->
                // 因 literal 产生新的行
                if (newline) {
                    print(children, i, compound.children.size, offset, level + commit, end = end)
                } else {
                    val length = if (offset == 8) command.name.length + 1 else commit + 1
                    print(children, i, compound.children.size, offset + length, level, end = end)
                }
            }
        }
        val size = context.commandCompound.children.size
        context.commandCompound.children.forEachIndexed { index, children ->
            print(children, index, size, end = index + 1 == size)
        }
        builder.lines().forEach {
            sender.sendMessage(it)
        }
    }
}