package taboolib.common.platform.command

class SimpleCommandBody(val func: CommandBuilder.CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    var permissionDefault: PermissionDefault = PermissionDefault.OP
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}