package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import java.lang.reflect.Field
import java.util.function.Supplier

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

class SimpleCommandBody(val func: CommandBuilder.CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}

@Awake
object SimpleCommandRegister : Injector.Classes, Injector.Fields {

    val body = HashMap<String, MutableList<SimpleCommandBody>>()

    fun loadBody(field: Field, instance: Supplier<*>): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)
            val obj = field.get(instance.get())
            return if (field.type == SimpleCommandBody::class.java) {
                (obj as SimpleCommandBody).apply {
                    name = field.name
                    aliases = annotation.aliases
                    optional = annotation.optional
                    permission = annotation.permission
                }
            } else {
                SimpleCommandBody().apply {
                    name = field.name
                    aliases = annotation.aliases
                    optional = annotation.optional
                    permission = annotation.permission
                    field.type.declaredFields.forEach {
                        it.isAccessible = true
                        children += loadBody(it, instance) ?: return@forEach
                    }
                }
            }
        }
        return null
    }

    override fun inject(clazz: Class<*>, instance: Supplier<*>) {
    }

    override fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>) {
        body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
    }

    override fun postInject(clazz: Class<*>, instance: Supplier<*>) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            val annotation = clazz.getAnnotation(CommandHeader::class.java)
            command(annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault) {
                body[clazz.name]?.forEach { body ->
                    fun register(body: SimpleCommandBody, component: CommandBuilder.CommandComponent) {
                        component.literal(body.name, *body.aliases, optional = body.optional, permission = body.permission) {
                            if (body.children.isEmpty()) {
                                body.func(this)
                            } else {
                                body.children.forEach { children ->
                                    register(children, this)
                                }
                            }
                        }
                    }
                    register(body, this)
                }
            }
        }
    }

    override val lifeCycle: LifeCycle
        get() = LifeCycle.ENABLE

    override val priority: Byte
        get() = 0

}
