package taboolib.common.inject

import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.io.getInstance
import taboolib.common.io.runningClasses
import taboolib.common.platform.AwakeFunction
import taboolib.common.platform.PlatformFactory
import java.util.*

/**
 * TabooLib
 * taboolib.common.inject.RuntimeInjector
 *
 * @author sky
 * @since 2021/6/24 3:58 下午
 */
object RuntimeInjector {

    private val priorityMap = TreeMap<Byte, Injectors>()

    init {
        LifeCycle.values().forEach { register(AwakeFunction(it)) }
    }

    fun lifeCycle(lifeCycle: LifeCycle) {
        if (TabooLibCommon.isKotlinEnvironment()) {
            val classes = runningClasses.filter { PlatformFactory.checkPlatform(it) }
            priorityMap.forEach {
                classes.forEach { c -> inject(c, it.value, lifeCycle) }
            }
        }
    }

    fun <T> injectAll(clazz: Class<T>) {
        priorityMap.forEach { inject(clazz, it.value, null) }
    }

    fun <T> inject(clazz: Class<T>, injectors: Injectors, lifeCycle: LifeCycle?) {
        if (TabooLibCommon.isStopped()) {
            return
        }
        val instance = clazz.getInstance() ?: return
        val declaredFields = clazz.declaredFields
        val declaredMethods = clazz.declaredMethods
        injectors.classes.forEach { inj ->
            if (lifeCycle == null || lifeCycle == inj.lifeCycle) {
                inj.inject(clazz, instance.get()!!)
            }
        }
        injectors.fields.forEach { inj ->
            if (lifeCycle == null || lifeCycle == inj.lifeCycle) {
                declaredFields.forEach {
                    it.isAccessible = true
                    inj.inject(it, clazz, instance.get()!!)
                }
            }
        }
        injectors.methods.forEach { inj ->
            if (lifeCycle == null || lifeCycle == inj.lifeCycle) {
                declaredMethods.forEach {
                    it.isAccessible = true
                    inj.inject(it, clazz, instance.get()!!)
                }
            }
        }
        injectors.classes.forEach { inj ->
            if (lifeCycle == null || lifeCycle == inj.lifeCycle) {
                inj.postInject(clazz, instance.get()!!)
            }
        }
    }

    fun register(injector: Injector.Fields) {
        priorityMap.computeIfAbsent(injector.priority) { Injectors() }.fields += injector
    }

    fun register(injector: Injector.Methods) {
        priorityMap.computeIfAbsent(injector.priority) { Injectors() }.methods += injector
    }

    fun register(injector: Injector.Classes) {
        priorityMap.computeIfAbsent(injector.priority) { Injectors() }.classes += injector
    }

    class Injectors {

        val classes = ArrayList<Injector.Classes>()
        val fields = ArrayList<Injector.Fields>()
        val methods = ArrayList<Injector.Methods>()
    }
}