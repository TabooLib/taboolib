package taboolib.common.platform.command

/**
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
) {

    val name = name.lowercase()
}