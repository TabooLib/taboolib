package taboolib.common.platform.command.component

class CommandComponentLiteral(index: Int, val aliases: Array<String>, optional: Boolean, permission: String) : CommandComponent(index, optional, permission) {

    override fun toString(): String {
        return "CommandComponentLiteral(aliases=${aliases.contentToString()})"
    }
}