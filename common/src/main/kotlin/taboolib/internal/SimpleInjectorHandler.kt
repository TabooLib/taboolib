package taboolib.internal

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.inject.InjectorFactory
import taboolib.common.platform.AwakeFunction
import java.util.*

/**
 * TabooLib
 * taboolib.internal.SimpleInjectorHandler
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
object SimpleInjectorHandler : InjectorFactory.Handler() {

    private val propertyMap = TreeMap<Byte, Injector>()

    init {
        LifeCycle.values().forEach { register(AwakeFunction(it)) }
    }

    override fun register(injector: Injector) {
        TODO("Not yet implemented")
    }

    override fun inject(lifeCycle: LifeCycle) {
        TODO("Not yet implemented")
    }

    override fun inject(target: Class<*>) {
        TODO("Not yet implemented")
    }

    override fun inject(target: Class<*>, lifeCycle: LifeCycle) {
        TODO("Not yet implemented")
    }
}