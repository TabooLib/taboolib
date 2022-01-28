package taboolib.common.platform.command

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import taboolib.common.io.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake

@Awake
@Bind([CommandHeader::class, CommandBody::class])
object SimpleCommandRegister : Injector(LifeCycle.ENABLE) {

    val main = HashMap<String, SimpleCommandMain>()
    val body = HashMap<String, MutableList<SimpleCommandBody>>()

    fun loadBody(field: ClassField, instance: InstGetter<*>): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)!!
            val obj = field.get(instance.get())
            return when (field.fieldType) {
                SimpleCommandMain::class.java -> {
                    null
                }
                SimpleCommandBody::class.java -> {
                    (obj as SimpleCommandBody).apply {
                        name = field.name
                        aliases = annotation.property("aliases")!!
                        optional = annotation.property("optional")!!
                        permission = annotation.property("permission")!!
                        permissionDefault = annotation.enum("permissionDefault")
                    }
                }
                else -> {
                    SimpleCommandBody().apply {
                        name = field.name
                        aliases = annotation.property("aliases")!!
                        optional = annotation.property("optional")!!
                        permission = annotation.property("permission")!!
                        permissionDefault = annotation.enum("permissionDefault")
                        ReflexClass.of(field.fieldType).structure.fields.forEach {
                            children += loadBody(it, instance) ?: return@forEach
                        }
                    }
                }
            }
        }
        return null
    }

    override fun inject(clazz: Class<*>, field: ClassField, instance: InstGetter<*>) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.fieldType == SimpleCommandMain::class.java) {
            main[clazz.name] = field.get(instance) as SimpleCommandMain
        } else {
            body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
        }
    }

    override fun postInject(clazz: Class<*>, instance: InstGetter<*>) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            val annotation = clazz.getAnnotation(CommandHeader::class.java)
            command(annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault,
                body[clazz.name]?.filter { it.permission.isNotEmpty() }?.associate { it.permission to it.permissionDefault } ?: emptyMap()
            ) {
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
}