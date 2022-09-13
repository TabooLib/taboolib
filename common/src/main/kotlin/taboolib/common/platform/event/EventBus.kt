package taboolib.common.platform.event

import org.tabooproject.reflex.ClassAnnotation
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Unknown
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.Ghost
import taboolib.common.platform.Platform
import taboolib.common.platform.function.*
import taboolib.common.util.optional
import java.util.function.Supplier

@Awake
class EventBus : ClassVisitor(0) {

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameter.size == 1) {
            val anno = method.getAnnotation(SubscribeEvent::class.java)
            val bind = anno.property("bind", "")
            val optionalEvent = if (bind.isNotEmpty()) {
                try {
                    Class.forName(bind)
                } catch (ex: Throwable) {
                    null
                }
            } else {
                null
            }
            if (method.parameterTypes.size != 1) {
                error("$clazz#${method.name} must have 1 parameter and must be an event type")
            }
            // 未找到事件类
            if (method.parameterTypes[0] == Unknown::class.java) {
                // 忽略警告
                if (!method.isAnnotationPresent(Ghost::class.java)) {
                    warning("${method.parameter[0].name} not found, use @Ghost to turn off this warning")
                }
                return
            }
            optional(anno) {
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
    }

    private fun registerBukkit(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val priority = event.enum<EventPriority>("priority", EventPriority.NORMAL)
        val ignoreCancelled = event.property("ignoreCancelled", false)
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBukkitListener(optionalBind, priority, ignoreCancelled) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerBukkitListener(method.parameterTypes[0], priority, ignoreCancelled) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerBungee(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val annoLevel = event.property("level", -1)
        val level = if (annoLevel != 0) annoLevel else event.enum<EventPriority>("priority", EventPriority.NORMAL).level
        val ignoreCancelled = event.property("ignoreCancelled", false)
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerBungeeListener(optionalBind, level, ignoreCancelled) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerBungeeListener(method.parameterTypes[0], level, ignoreCancelled) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerVelocity(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val postOrder = event.enum<PostOrder>("postOrder", PostOrder.NORMAL)
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerVelocityListener(optionalBind, postOrder) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerVelocityListener(method.parameterTypes[0], postOrder) {
                invoke(obj, method, it)
            }
        }
    }

    private fun registerSponge(method: ClassMethod, optionalBind: Class<*>?, event: ClassAnnotation, obj: Any?) {
        val order = event.enum<EventOrder>("order", EventOrder.DEFAULT)
        val beforeModifications = event.property("beforeModifications", false)
        if (method.parameterTypes[0] == OptionalEvent::class.java) {
            if (optionalBind != null) {
                registerSpongeListener(optionalBind, order, beforeModifications) {
                    invoke(obj, method, it, true)
                }
            }
        } else {
            registerSpongeListener(method.parameterTypes[0], order, beforeModifications) {
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