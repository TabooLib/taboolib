package taboolib.common.platform.command

import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic

/**
 * 添加一层世界节点（自动约束、自动建议）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.world(
    comment: String = "world",
    suggest: List<String> = listOf("~"),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment, optional, permission, dynamic).suggestWorlds(suggest)
}

/**
 * 添加一层坐标 X,Y,Z 节点（自动约束、自动建议）
 */
fun CommandComponent.xyz(
    x: String = "x",
    y: String = "y",
    z: String = "z",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return decimal(x, optional = optional, permission = permission)
        .suggestionUncheck<ProxyPlayer> { sender, _ -> of("~", sender.location.x) }
        .decimal(y)
        .suggestionUncheck<ProxyPlayer> { sender, _ -> of("~", sender.location.y) }
        .decimal(z, dynamic = dynamic)
        .suggestionUncheck<ProxyPlayer> { sender, _ -> of("~", sender.location.z) }
}

/**
 * 添加一层坐标 YAW,PITCH 节点（自动约束、自动建议）
 */
fun CommandComponent.euler(
    yaw: String = "yaw",
    pitch: String = "pitch",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return decimal(yaw, optional = optional, permission = permission)
        .suggestionUncheck<ProxyPlayer> { sender, _ -> of("~", sender.location.yaw) }
        .decimal(pitch, dynamic = dynamic)
        .suggestionUncheck<ProxyPlayer> { sender, _ -> of("~", sender.location.pitch) }
}

/**
 * 添加一层坐标节点（自动约束、自动建议）
 */
fun CommandComponent.location(
    world: String = "world",
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    euler: Boolean = true,
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return world(world, optional = optional, permission = permission).xyz(x, y, z, dynamic = dynamic).also {
        if (euler) {
            it.euler(yaw, pitch, dynamic = dynamic)
        }
    }
}

/**
 * 添加一层坐标节点（自动约束、自动建议）
 */
fun CommandComponent.locationWithoutWorld(
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    euler: Boolean = true,
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return xyz(x, y, z, optional, permission, dynamic).also {
        if (euler) {
            it.euler(yaw, pitch, dynamic = dynamic)
        }
    }
}

private fun of(vararg elements: Any): List<String> {
    return if (elements.isNotEmpty()) elements.map { it.toString() } else emptyList()
}
