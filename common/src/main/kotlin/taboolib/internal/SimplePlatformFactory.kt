package taboolib.internal

import taboolib.common.platform.PlatformFactory

/**
 * TabooLib
 * taboolib.internal.SimplePlatformFactory
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
object SimplePlatformFactory : PlatformFactory {

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        TODO("Not yet implemented")
    }

    override fun checkPlatform(clazz: Class<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAwakeInstances(): List<Any> {
        TODO("Not yet implemented")
    }

    override fun getPlatformServices(): List<Any> {
        TODO("Not yet implemented")
    }

    override fun <T> getAwakeInstance(clazz: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T> getPlatformService(clazz: Class<T>): T {
        TODO("Not yet implemented")
    }
}