package taboolib.common.inject

import taboolib.common.LifeCycle
import taboolib.common.boot.SimpleServiceLoader
import taboolib.internal.SimpleInjectorFactory

/**
 * TabooLib
 * taboolib.common.inject.InjectorFactory
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
interface InjectorFactory {

    fun register(injector: Injector)

    /**
     * 向所有类注入给定生命周期阶段
     */
    fun inject(lifeCycle: LifeCycle)

    /**
     * 向特定类注入给定生命周期阶段，若 LifeCycle 为空则跳过生命周期判断
     */
    fun inject(target: Class<*>, lifeCycle: LifeCycle? = null)

    companion object {

        @JvmField
        val INSTANCE: InjectorFactory = SimpleServiceLoader.load(InjectorFactory::class.java) { SimpleInjectorFactory }
    }
}