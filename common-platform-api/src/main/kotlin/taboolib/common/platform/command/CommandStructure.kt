package taboolib.common.platform.command

/**
 * TabooLib
 * taboolib.common.CommandStructure
 *
 * @author sky
 * @since 2021/6/24 11:48 下午
 */
class CommandStructure(
    name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val permissionMessage: String,
    val permissionDefault: PermissionDefault,
    val permissionChildren: Map<String, PermissionDefault>,
    val newParser: Boolean,
) {

    val name = name.lowercase()
}