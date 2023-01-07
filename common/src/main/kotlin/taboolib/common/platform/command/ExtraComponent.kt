@file:Isolated
package taboolib.common.platform.command

import taboolib.common.Isolated
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic

/**
 * 添加一层整型节点（自动约束）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.int(
    comment: String = "int",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).also {
        // 如果没有额外建议则约束参数输入
        if (suggest.isEmpty()) {
            it.restrictInt()
        } else {
            it.suggestUncheck { suggest }
        }
    }
}

/**
 * 添加一层数字节点（自动约束）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.decimal(
    comment: String = "decimal",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).also {
        // 如果没有额外建议则约束参数输入
        if (suggest.isEmpty()) {
            it.restrictDouble()
        } else {
            it.suggestUncheck { suggest }
        }
    }
}

/**
 * 添加一层布尔值节点（自动约束、自动建议）
 */
fun CommandComponent.bool(
    comment: String = "boolean",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).suggestBoolean()
}

/**
 * 添加一层玩家节点（自动约束、自动建议）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.player(
    comment: String = "player",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).suggestPlayers(suggest)
}