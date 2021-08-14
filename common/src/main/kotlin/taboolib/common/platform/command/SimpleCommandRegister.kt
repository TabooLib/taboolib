package taboolib.common.platform.command

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import java.lang.reflect.Field
import java.util.function.Supplier

@Awake
object SimpleCommandRegister : Injector.Classes, Injector.Fields {

    val main = HashMap<String, SimpleCommandMain>()
    val body = HashMap<String, MutableList<SimpleCommandBody>>()

    fun loadBody(field: Field, instance: Supplier<*>): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)
            val obj = field.get(instance.get())
            return when (field.type) {
                SimpleCommandMain::class.java -> {
                    null
                }
                SimpleCommandBody::class.java -> {
                    (obj as SimpleCommandBody).apply {
                        name = field.name
                        aliases = annotation.aliases
                        optional = annotation.optional
                        permission = annotation.permission
                    }
                }
                else -> {
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
        }
        return null
    }

    override fun inject(clazz: Class<*>, instance: Supplier<*>) {
    }

    override fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.type == SimpleCommandMain::class.java) {
            main[clazz.name] = field.get(instance) as SimpleCommandMain
        } else {
            body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
        }
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
                main[clazz.name]?.func?.invoke(this)
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