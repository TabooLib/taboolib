package taboolib.expansion

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.module.lang.asLangText

fun CommandComponent.createHelper(checkPermissions: Boolean = true) {
    execute<ProxyCommandSender> { sender, context, _ ->
        val command = context.command
        val builder = StringBuilder("§cUsage: /${command.name}")
        var newline = false

        fun check(children: List<CommandComponent>): List<CommandComponent> {
            // 检查权限
            val filterChildren = if (checkPermissions) {
                children.filter { sender.hasPermission(it.permission) }
            } else {
                children
            }
            // 过滤隐藏
            return filterChildren.filter { it !is CommandComponentLiteral || !it.hidden }
        }

        fun print(compound: CommandComponent, index: Int, size: Int, offset: Int = 8, level: Int = 0, end: Boolean = false, optional: Boolean = false) {
            var option = optional
            var comment = 0
            when (compound) {
                is CommandComponentLiteral -> {
                    if (size == 1) {
                        builder.append(" ").append("§c${compound.aliases[0]}")
                    } else {
                        newline = true
                        builder.appendLine()
                        builder.append(CommandHelper.space(offset))
                        if (level > 1) {
                            builder.append(if (end) " " else "§7│")
                        }
                        builder.append(CommandHelper.space(level))
                        if (index + 1 < size) {
                            builder.append("§7├── ")
                        } else {
                            builder.append("§7└── ")
                        }
                        builder.append("§c${compound.aliases[0]}")
                    }
                    option = false
                    comment = compound.aliases[0].length
                }

                is CommandComponentDynamic -> {
                    val value = if (compound.comment.startsWith("@")) {
                        sender.asLangText(compound.comment.substring(1))
                    } else {
                        compound.comment
                    }
                    comment = if (compound.optional || option) {
                        option = true
                        builder.append(" ").append("§8[<$value>]")
                        compound.comment.length + 4
                    } else {
                        builder.append(" ").append("§7<$value>")
                        compound.comment.length + 2
                    }
                }
            }
            if (level > 0) {
                comment += 1
            }
            val checkedChildren = check(compound.children)
            checkedChildren.forEachIndexed { i, children ->
                // 因 literal 产生新的行
                if (newline) {
                    print(children, i, checkedChildren.size, offset, level + comment, end, option)
                } else {
                    val length = if (offset == 8) command.name.length + 1 else comment + 1
                    print(children, i, checkedChildren.size, offset + length, level, end, option)
                }
            }
        }
        val checkedChildren = check(context.commandCompound.children)
        val size = checkedChildren.size
        checkedChildren.forEachIndexed { index, children ->
            print(children, index, size, end = index + 1 == size)
        }
        builder.lines().forEach {
            sender.sendMessage(it)
        }
    }
}


/**
 * 创建命令描述帮助
 */
fun CommandComponent.createDescriptionHelper(checkPermissions: Boolean = true) {
    execute<ProxyCommandSender> { sender, context, _ ->
        val command = context.command
        val rootChildren = context.commandCompound.children
            .filter { !checkPermissions || sender.hasPermission(it.permission) }
            .filter { it !is CommandComponentLiteral || !it.hidden }
        val text =
            CommandHelper.buildDescriptionHelperText(command.name, rootChildren) { key -> sender.asLangText(key) }
        text.lines().forEach { sender.sendMessage(it) }
    }
}

object CommandHelper {
    fun space(n: Int) = (1..n).joinToString("") { " " }

    /**
     * 构建命令帮助文本（纯字符串，不依赖平台 sender）
     *
     * @param commandName 命令名称
     * @param rootChildren 根节点的子节点列表（已过滤权限和隐藏）
     * @param langResolver 将 @key 风格的注释解析为实际文本，默认原样返回
     */
    fun buildHelperText(
        commandName: String,
        rootChildren: List<CommandComponent>,
        langResolver: (String) -> String = { it }
    ): String {
        val builder = StringBuilder("§cUsage: /$commandName")
        var newline = false

        fun print(
            compound: CommandComponent,
            index: Int,
            size: Int,
            offset: Int = 8,
            level: Int = 0,
            end: Boolean = false,
            optional: Boolean = false
        ) {
            var option = optional
            var comment = 0
            when (compound) {
                is CommandComponentLiteral -> {
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
                    option = false
                    comment = compound.aliases[0].length
                }

                is CommandComponentDynamic -> {
                    val value = if (compound.comment.startsWith("@")) {
                        langResolver(compound.comment.substring(1))
                    } else {
                        compound.comment
                    }
                    comment = if (compound.optional || option) {
                        option = true
                        builder.append(" ").append("§8[<$value>]")
                        compound.comment.length + 4
                    } else {
                        builder.append(" ").append("§7<$value>")
                        compound.comment.length + 2
                    }
                }
            }
            if (level > 0) comment += 1
            val children = compound.children.filter { it !is CommandComponentLiteral || !it.hidden }
            children.forEachIndexed { i, child ->
                if (newline) {
                    print(child, i, children.size, offset, level + comment, end, option)
                } else {
                    val length = if (offset == 8) commandName.length + 1 else comment + 1
                    print(child, i, children.size, offset + length, level, end, option)
                }
            }
        }

        val size = rootChildren.size
        rootChildren.forEachIndexed { index, child ->
            print(child, index, size, end = index + 1 == size)
        }
        return builder.toString()
    }


    /**
     * 构建带描述的命令帮助文本（纯字符串，不依赖平台 sender）
     *
     * @param commandName 命令名称
     * @param rootChildren 根节点的子节点列表（已过滤权限和隐藏）
     * @param langResolver 将 @key 风格的注释/描述解析为实际文本，默认原样返回
     */
    fun buildDescriptionHelperText(
        commandName: String,
        rootChildren: List<CommandComponent>,
        langResolver: (String) -> String = { it }
    ): String {
        val builder = StringBuilder("§cUsage: /$commandName")
        var newline = false

        fun space(n: Int) = (1..n).joinToString("") { " " }

        fun resolveDesc(desc: String): String =
            if (desc.startsWith("@")) langResolver(desc.substring(1)) else desc

        fun print(
            compound: CommandComponent,
            index: Int,
            size: Int,
            offset: Int = 8,
            level: Int = 0,
            end: Boolean = false,
            optional: Boolean = false,
            parentDescription: String = ""
        ) {
            var option = optional
            var comment = 0
            var currentDescription = parentDescription
            when (compound) {
                is CommandComponentLiteral -> {
                    if (size == 1) {
                        builder.append(" ").append("§c${compound.aliases[0]}")
                        currentDescription = compound.description
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
                        currentDescription = compound.description
                    }
                    option = false
                    comment = compound.aliases[0].length
                }

                is CommandComponentDynamic -> {
                    val value = if (compound.comment.startsWith("@")) {
                        langResolver(compound.comment.substring(1))
                    } else {
                        compound.comment
                    }
                    comment = if (compound.optional || option) {
                        option = true
                        builder.append(" ").append("§8[<$value>]")
                        compound.comment.length + 4
                    } else {
                        builder.append(" ").append("§7<$value>")
                        compound.comment.length + 2
                    }
                    if (compound.description.isNotEmpty()) {
                        currentDescription = compound.description
                    }
                }
            }
            if (level > 0) comment += 1
            val children = compound.children.filter { it !is CommandComponentLiteral || !it.hidden }
            if (children.isEmpty() && currentDescription.isNotEmpty()) {
                builder.append(" §7- §c${resolveDesc(currentDescription)}")
            }
            children.forEachIndexed { i, child ->
                if (newline) {
                    print(child, i, children.size, offset, level + comment, end, option, currentDescription)
                } else {
                    val length = if (offset == 8) commandName.length + 1 else comment + 1
                    print(child, i, children.size, offset + length, level, end, option, currentDescription)
                }
            }
        }

        val size = rootChildren.size
        rootChildren.forEachIndexed { index, child ->
            print(child, index, size, end = index + 1 == size)
        }
        return builder.toString()
    }

}
