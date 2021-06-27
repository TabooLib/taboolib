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

    private val injectFields = ArrayList<Injector.Fields>()
    private val injectMethods = ArrayList<Injector.Methods>()
    private val injectClasses = ArrayList<Injector.Classes>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            runningClasses.filter { PlatformFactory.checkPlatform(it) }.forEach { inject(it) }
        }
    }

    fun <T> inject(clazz: Class<T>, new: Boolean = false): T? {
        val instance = clazz.getInstance(new = new) ?: return null
        val declaredFields = clazz.declaredFields
        val declaredMethods = clazz.declaredMethods
        injectFields.forEach { inj -> declaredFields.forEach { inj.inject(it, clazz, instance) } }
        injectMethods.forEach { inj -> declaredMethods.forEach { inj.inject(it, clazz, instance) } }
        injectClasses.forEach { it.inject(clazz, instance) }
        return instance
    }

    fun register(injector: Injector.Fields) {
        injectFields += injector
        injectFields.sortedBy { it.priority }
    }

    fun register(injector: Injector.Methods) {
        injectMethods += injector
        injectMethods.sortedBy { it.priority }
    }

    fun register(injector: Injector.Classes) {
        injectClasses += injector
        injectClasses.sortedBy { it.priority }
    }
}