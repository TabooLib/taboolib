@file:Suppress("UNCHECKED_CAST")

package taboolib.internal

import taboolib.common.InstGetter
import taboolib.common.TabooLib
import taboolib.common.boot.Mechanism
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.Injector
import taboolib.common.inject.InjectorHandler
import taboolib.common.io.findInstance
import taboolib.common.io.runningClasses
import taboolib.common.platform.*
import taboolib.common.platform.function.unregisterCommands
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.internal.SimplePlatformFactory
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
open class SimplePlatformFactory : PlatformFactory, Mechanism {

    val awokenMap = ConcurrentHashMap<String, InstGetter<*>>()
    val serviceMap = ConcurrentHashMap<String, InstGetter<*>>()

    override fun startup() {
        setupEnv()
        setupAwake()
    }

    override fun shutdown() {
        kotlin.runCatching { unregisterCommands() }
        kotlin.runCatching { getAwakeInstances().filterIsInstance<Releasable>().forEach { it.release() } }
    }

    override fun checkPlatform(clazz: Class<*>): Boolean {
        return TabooLib.runningPlatform() in (clazz.getAnnotation(PlatformSide::class.java) ?: return true).value
    }

    override fun getAwakeInstances(): List<Any> {
        return awokenMap.values.mapNotNull { it.get() }
    }

    override fun getPlatformServices(): List<Any> {
        return serviceMap.values.mapNotNull { it.get() }
    }

    override fun <T> getAwakeInstance(clazz: Class<T>): T? {
        return awokenMap[clazz.name]?.get() as T?
    }

    override fun <T> getPlatformService(clazz: Class<T>): T? {
        return serviceMap[clazz.name]?.get() as T?
    }

    fun setupEnv() {
        runningClasses.filter { checkPlatform(it) }.forEach { RuntimeEnv.INSTANCE.load(it) }
    }

    fun setupAwake() {
        runningClasses.forEach {
            if (checkPlatform(it) && it.isAnnotationPresent(Awake::class.java)) {
                val interfaces = it.interfaces
                val instance = it.findInstance(true)
                if (interfaces.contains(Injector::class.java)) {
                    InjectorHandler.INSTANCE.register(instance.get() as Injector)
                }
                interfaces.forEach { int ->
                    if (int.isAnnotationPresent(PlatformService::class.java)) {
                        serviceMap[int.name] = instance
                    }
                }
                awokenMap[it.name] = instance
            }
            if (it.isAnnotationPresent(PlatformImplementation::class.java) && it.getAnnotation(PlatformImplementation::class.java).platform == TabooLib.runningPlatform()) {
                val interfaces = it.interfaces
                if (interfaces.isNotEmpty()) {
                    awokenMap[interfaces[0].name] = it.findInstance(true)
                }
            }
        }
    }
}