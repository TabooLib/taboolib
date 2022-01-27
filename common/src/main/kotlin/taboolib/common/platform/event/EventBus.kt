package taboolib.common.platform.event

import org.tabooproject.reflex.ClassMethod
import taboolib.common.InstGetter
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.*

@Awake
@Bind([SubscribeEvent::class], target = Bind.Target.METHOD)
object EventBus : Injector(LifeCycle.ENABLE) {

    override fun inject(clazz: Class<*>, method: ClassMethod, instance: InstGetter<*>) {
        if (method.parameter.size == 1) {
            val event = method.getAnnotation(SubscribeEvent::class.java)!!
            val bind = event.property<String>("bind")
            val eventBind = if (bind != null) {
                try {
                    Class.forName(bind)
                } catch (ex: Throwable) {
                    null
                }
            } else {
                null
            }
            val obj = instance.get() ?: return
            when (TabooLib.runningPlatform()) {
                Platform.BUKKIT, Platform.NUKKIT -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerBukkitListener(eventBind, event.enum("priority"), event.property("ignoreCancelled")!!) {
                                method.invoke(obj, OptionalEvent(it))
                            }
                        }
                    } else {
                        registerBukkitListener(method.parameterTypes[0], event.enum("priority"), event.property("ignoreCancelled")!!) {
                            method.invoke(obj, it)
                        }
                    }
                }
                Platform.BUNGEE -> {
                    val level = if (event.property<Int>("level") != null) {
                        event.property<Int>("level")!!
                    } else {
                        event.enum<EventPriority>("priority").level
                    }
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerBungeeListener(eventBind, level, event.property("ignoreCancelled")!!) { method.invoke(obj, OptionalEvent(it)) }
                        }
                    } else {
                        registerBungeeListener(method.parameterTypes[0], level, event.property("ignoreCancelled")!!) { method.invoke(obj, it) }
                    }
                }
                Platform.VELOCITY -> {
                    if (method.parameterTypes[0] == OptionalEvent::class.java) {
                        if (eventBind != null) {
                            registerVelocityListener(eventBind, event.enum("postOrder")) {
                                method.invoke(obj, OptionalEvent(it))
                            }
                        }
                    } else {
                        registerVelocityListener(method.parameterTypes[0], event.enum("postOrder")) {
                            method.invoke(obj, it)
                        }
                    }
                }
                Platform.SPONGE_API_7, Platform.SPONGE_API_8 -> {
                    postpone {
                        if (method.parameterTypes[0] == OptionalEvent::class.java) {
                            if (eventBind != null) {
                                registerSpongeListener(eventBind, event.enum("order"), event.property("beforeModifications")!!) {
                                    method.invoke(obj, OptionalEvent(it))
                                }
                            }
                        } else {
                            registerSpongeListener(method.parameterTypes[0], event.enum("order"), event.property("beforeModifications")!!) {
                                method.invoke(obj, it)
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
    }
}