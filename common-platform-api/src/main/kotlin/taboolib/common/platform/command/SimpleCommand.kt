package taboolib.common.platform.command

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.ExecuteContext
import taboolib.common.reflect.getAnnotationIfPresent
import taboolib.common.reflect.hasAnnotation
import java.util.function.Supplier

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val permissionMessage: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val newParser: Boolean = false,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val hidden: Boolean = false,
)

fun mainCommand(func: CommandBase.() -> Unit): SimpleCommandMain {
    return SimpleCommandMain(func)
}

fun subCommand(func: CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}

inline fun <reified T> subCommandExec(crossinline func: ExecuteContext<T>.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody { exec<T> { func() } }
}

class SimpleCommandMain(val func: CommandBase.() -> Unit = {})

class SimpleCommandBody(val func: CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    var permissionDefault: PermissionDefault = PermissionDefault.OP
    var hidden = false
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}

@Suppress("DuplicatedCode")
@Awake
class SimpleCommandRegister : ClassVisitor(0) {

    val main = HashMap<String, SimpleCommandMain>()
    val body = HashMap<String, MutableList<SimpleCommandBody>>()

    fun loadBody(field: ClassField, instance: Supplier<*>?): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)
            val obj = field.get(instance?.get())
            return when (field.fieldType) {
                SimpleCommandMain::class.java -> {
                    null
                }
                SimpleCommandBody::class.java -> {
                    (obj as SimpleCommandBody).apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                    }
                }
                else -> {
                    SimpleCommandBody().apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                        // 向下搜索字段
                        ReflexClass.of(field.fieldType).structure.fields.forEach {
                            children += loadBody(it, instance) ?: return@forEach
                        }
                    }
                }
            }
        }
        return null
    }

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.fieldType == SimpleCommandMain::class.java) {
            main[clazz.name] = field.get(instance?.get()) as SimpleCommandMain
        } else {
            body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
        }
    }

    override fun visitEnd(clazz: Class<*>, instance: Supplier<*>?) {
        if (clazz.hasAnnotation(CommandHeader::class.java)) {
            val annotation = clazz.getAnnotationIfPresent(CommandHeader::class.java)!!
            command(annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault,
                body[clazz.name]?.filter { it.permission.isNotEmpty() }?.associate { it.permission to it.permissionDefault } ?: emptyMap(),
                annotation.newParser,
            ) {
                main[clazz.name]?.func?.invoke(this)
                body[clazz.name]?.forEach { body ->
                    fun register(body: SimpleCommandBody, component: CommandComponent) {
                        component.literal(body.name, *body.aliases, optional = body.optional, permission = body.permission, hidden = body.hidden) {
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

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }
}