package taboolib.common.platform.command

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP
)