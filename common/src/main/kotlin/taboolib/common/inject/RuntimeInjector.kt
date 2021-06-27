package taboolib.common.inject

import taboolib.common.TabooLibCommon
import taboolib.common.io.runningClasses
import taboolib.common.io.getInstance
import taboolib.common.platform.PlatformFactory

/**
 * TabooLib
 * taboolib.common.inject.RuntimeInjector
 *
 * @author sky
 * @since 2021/6/24 3:58 下午
 */
object RuntimeInjector {

    private val priorityMap = HashMap<Byte, Injectors>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            val classes = runningClasses.filter { PlatformFactory.checkPlatform(it) }
            priorityMap.keys.sorted().forEach {
                classes.forEach { c -> inject(c, priorityMap[it]!!) }
            }
        }
    }

    fun <T> injectAll(clazz: Class<T>) {
        priorityMap.keys.sorted().forEach { inject(clazz, priorityMap[it]!!) }
    }

    fun <T> inject(clazz: Class<T>, injectors: Injectors): T? {
        val instance = clazz.getInstance(new = false) ?: return null
        val declaredFields = clazz.declaredFields
        val declaredMethods = clazz.declaredMethods
        injectors.fields.forEach { inj -> declaredFields.forEach { inj.inject(it, clazz, instance) } }
        injectors.methods.forEach { inj -> declaredMethods.forEach { inj.inject(it, clazz, instance) } }
        injectors.classes.forEach { it.inject(clazz, instance) }
        return instance
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

        val fields = ArrayList<Injector.Fields>()
        val methods = ArrayList<Injector.Methods>()
        val classes = ArrayList<Injector.Classes>()
    }
}