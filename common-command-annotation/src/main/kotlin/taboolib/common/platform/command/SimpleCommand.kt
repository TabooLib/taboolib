package taboolib.common.platform.command

class SimpleCommandMain(val func: Component.() -> Unit = {})

class SimpleCommandBody(val func: Section.() -> Unit = {}) {

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