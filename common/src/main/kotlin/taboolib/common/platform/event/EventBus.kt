package taboolib.common.platform.event

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.*
import java.util.function.Supplier

@Awake
class EventBus : ClassVisitor(0) {

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameter.size == 1) {
            val anno = method.getAnnotation(SubscribeEvent::class.java)!!
            val bind = anno.property<String>("bind").toString()
            val optionalEvent = if (bind.isNotEmpty()) {
                try {
                    Class.forName(bind)
                } catch (ex: Throwable) {
                    null
                }
            } else {
                null
            }
            val obj = instance?.get()
            // 判定运行平台
            when (runningPlatform) {
                // bk 平台与 nk 平台使用相同参数
                Platform.BUKKIT, Platform.NUKKIT -> {
                    registerBukkit(method, optionalEvent, anno, obj)
                }
                Platform.BUNGEE -> {
                    registerBungee(method, optionalEvent, anno, obj)
                }
                Platform.VELOCITY -> {
                    registerVelocity(method, optionalEvent, anno, obj)
                }
                Platform.SPONGE_API_7, Platform.SPONGE_API_8 -> {
                    postpone { registerSponge(method, optionalEvent, anno, obj) }
                }
                else -> {
                }
            }
        }
    }

    private fun registerBukkit(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?, ) {
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBukkitListener(optionalBind, event.property("priority")!!, event.property("ignoreCancelled")!!) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerBukkitListener(method.parameterTypes[0], event.property("priority")!!, event.property("ignoreCancelled")!!) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerBungee(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val level = if (event.property<Int>("level") != 0) event.property<Int>("level")!! else event.enum<EventPriority>("priority").level
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBungeeListener(optionalBind, level, event.property("ignoreCancelled")!!) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerBungeeListener(method.parameterTypes[0], level, event.property("ignoreCancelled")!!) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerVelocity(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerVelocityListener(optionalBind, event.enum("postOrder")) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerVelocityListener(method.parameterTypes[0], event.enum("postOrder")) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerSponge(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerSpongeListener(optionalBind, event.enum("order"), event.property("beforeModifications")!!) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerSpongeListener(method.parameterTypes[0], event.enum("order"), event.property("beforeModifications")!!) {
                invoke(obj, method, it)
            }
        }
    }

    private fun invoke(obj: Any?, method: ClassMethod, it: Any, optional: Boolean = false) {
        if (obj != null) {
            method.invoke(obj, if (optional) OptionalEvent(it) else it)
        } else {
            method.invokeStatic(if (optional) OptionalEvent(it) else it)
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }
}