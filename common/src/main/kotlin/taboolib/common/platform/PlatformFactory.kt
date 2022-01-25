package taboolib.common.platform

import taboolib.common.boot.SimpleServiceLoader
import taboolib.internal.SimplePlatformFactory

/**
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
interface PlatformFactory {

    fun init()

    fun cancel()

    fun getAwakeInstances(): List<Any>

    fun getPlatformServices(): List<Any>

    fun checkPlatform(clazz: Class<*>): Boolean

    fun <T> getAwakeInstance(clazz: Class<T>): T

    fun <T> getPlatformService(clazz: Class<T>): T

    companion object {

        @JvmField
        val INSTANCE: PlatformFactory = SimpleServiceLoader.load(PlatformFactory::class.java) { SimplePlatformFactory }

        inline fun <reified T> checkPlatform(): Boolean {
            return INSTANCE.checkPlatform(T::class.java)
        }

        inline fun <reified T> getAwakeInstance(): T {
            return INSTANCE.getAwakeInstance(T::class.java)
        }

        inline fun <reified T> getPlatformService(): T {
            return INSTANCE.getPlatformService(T::class.java)
        }
    }
}