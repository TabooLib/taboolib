package taboolib.internal

import org.tabooproject.reflex.ReflexClass
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.Bind
import taboolib.common.inject.InjectHandler
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetterException
import taboolib.common.io.findInstance
import taboolib.common.io.runningClasses
import taboolib.common.platform.SkipTo
import kotlin.collections.ArrayList

/**
 * TabooLib
 * taboolib.internal.SimpleInjectorHandler
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
@Internal
class SimpleInjectHandler : InjectHandler {

    private val injectors = ArrayList<RegisteredInjector>()

    init {
        LifeCycle.values().forEach { register(InjectorAwake(it)) }
    }

    override fun register(injector: Injector) {
        injectors += RegisteredInjector(injector)
        injectors.sortBy { it.injector.priority }
    }

    override fun inject(lifeCycle: LifeCycle) {
        injectors.forEach { i -> runningClasses.forEach { inject(it, lifeCycle, i) } }
    }

    override fun inject(target: Class<*>, lifeCycle: LifeCycle?) {
        injectors.forEach { inject(target, lifeCycle, it) }
    }

    private fun inject(target: Class<*>, lifeCycle: LifeCycle?, reg: RegisteredInjector) {
        val shutdown = TabooLib.monitor().isShutdown
        val skip = lifeCycle != null && (lifeCycle != reg.injector.lifeCycle || skipLevel(target) > lifeCycle.ordinal)

        val instance = target.findInstance()
        val isIge = instance is InstGetterException

        if (shutdown || skip || isIge) return

        if (reg.checkTarget(Bind.Target.CLASS) && reg.check(target)) {
            reg.injector.preInject(target, instance)
        }

        if (reg.checkTarget(Bind.Target.FIELD)) {
            ReflexClass.of(target)
                .structure.fields
                .asSequence()
                .filter { reg.check(it) }
                .forEach { reg.injector.inject(target, it, instance) }
        }

        if (reg.checkTarget(Bind.Target.METHOD)) {
            ReflexClass.of(target)
                .structure.methods
                .asSequence()
                .filter { reg.check(it) }
                .forEach { reg.injector.inject(target, it, instance) }
        }

        if (reg.checkTarget(Bind.Target.CLASS) && reg.check(target)) {
            reg.injector.postInject(target, instance)
        }
    }

    private fun skipLevel(clazz: Class<*>): Int {
        return if (clazz.isAnnotationPresent(SkipTo::class.java)) clazz.getAnnotation(SkipTo::class.java).value.ordinal else -1
    }
}
