package taboolib.common.inject

import taboolib.common.LifeCycle
import taboolib.common.boot.SimpleServiceLoader

/**
 * TabooLib
 * taboolib.common.inject.InjectorHandler
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
interface InjectorHandler {

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
        val INSTANCE: InjectorHandler = SimpleServiceLoader.load(InjectorHandler::class.java)
    }
}