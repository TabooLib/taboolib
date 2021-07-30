package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import java.lang.reflect.Field

fun command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.FALSE,
    commandBuilder: CommandBuilder.CommandBase.() -> Unit,
) {
    registerCommand(
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault),
        object : CommandExecutor {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                return CommandBuilder.CommandBase().also(commandBuilder).execute(CommandContext(sender, command, name, args))
            }
        },
        object : CommandCompleter {

            override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                return CommandBuilder.CommandBase().also(commandBuilder).suggest(CommandContext(sender, command, name, args))
            }
        },
        commandBuilder
    )
}

fun subCommand(func: CommandBuilder.CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}

@Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val permissionMessage: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.FALSE,
)

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
)

class SimpleCommandBody(val func: CommandBuilder.CommandComponent.() -> Unit) {

    var info: CommandBody? = null
}

@Awake
object SimpleCommandRegister : Injector.Classes, Injector.Fields {

    val body = HashMap<String, SimpleCommandBody>()

    override fun inject(clazz: Class<*>, instance: Any) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            body.clear()
        }
    }

    override fun inject(field: Field, clazz: Class<*>, instance: Any) {
        if (field.type == SimpleCommandBody::class.java) {
            val commandBody = field.get(instance) as SimpleCommandBody
            if (clazz.isAnnotationPresent(CommandBody::class.java)) {
                commandBody.info = clazz.getAnnotation(CommandBody::class.java)
            }
            body[field.name] = commandBody
        }
    }

    override fun postInject(clazz: Class<*>, instance: Any) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            val annotation = clazz.getAnnotation(CommandHeader::class.java)
            command(annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault) {
                body.forEach {
                    val info = it.value.info
                    literal(it.key, *info?.aliases ?: emptyArray(), optional = info?.optional ?: false, permission = info?.permission ?: "") {
                        it.value.func(this)
                    }
                }
            }
        }
    }

    override val lifeCycle: LifeCycle
        get() = LifeCycle.ENABLE

    override val priority: Byte
        get() = 0

}
