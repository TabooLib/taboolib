package taboolib.common.platform.command

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import java.util.function.Supplier

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
                    }
                }
                else -> {
                    SimpleCommandBody().apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
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

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }
}