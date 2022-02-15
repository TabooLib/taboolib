@file:Suppress("UNCHECKED_CAST")

package taboolib.internal

import taboolib.common.TabooLib
import taboolib.common.boot.Mechanism
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.InjectHandler
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetter
import taboolib.common.io.findInstance
import taboolib.common.io.runningClasses
import taboolib.common.platform.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.internal.SimplePlatformFactory
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
@Internal
class SimplePlatformFactory : PlatformFactory, Mechanism {

    val awokenMap = ConcurrentHashMap<String, InstGetter<*>>()
    val serviceMap = ConcurrentHashMap<String, InstGetter<*>>()

    override fun startup() {
        setupEnv()
        setupAwake()
    }

    override fun shutdown() {
        kotlin.runCatching { getAwakeInstances().filterIsInstance<Releasable>().forEach { it.release() } }
    }

    override fun checkPlatform(clazz: Class<*>): Boolean {
        val annotated = clazz.getAnnotation(PlatformSide::class.java) ?: return true
        return TabooLib.runningPlatform() in annotated.value
    }

    override fun getAwakeInstances() =
        awokenMap.values.mapNotNull { it.get() }

    override fun getPlatformServices() =
        serviceMap.values.mapNotNull { it.get() }

    override fun <T> getAwakeInstance(clazz: Class<T>) =
        awokenMap[clazz.name]?.get() as T?

    override fun <T> getPlatformService(clazz: Class<T>) =
        serviceMap[clazz.name]?.get() as T?

    private fun setupEnv() {
        kotlin.runCatching {
            runningClasses
                .filter { checkPlatform(it) }
                .forEach { RuntimeEnv.INSTANCE.load(it) }
        }
    }

    private fun setupAwake() {
        runningClasses.parallelStream()
            .filter { checkPlatform(it) }
            .filter { it.isAnnotationPresent(Awake::class.java) }
            .forEach { clazz ->
                val interfaces = clazz.interfaces
                val instance = clazz.findInstance(true)

                if (interfaces.contains(Injector::class.java)) {
                    InjectHandler.INSTANCE.register(instance.get() as Injector)
                }

                interfaces
                    .filter { it.isAnnotationPresent(PlatformService::class.java) }
                    .forEach { serviceMap[it.name] = instance }

                awokenMap[clazz.name] = instance
            }

        runningClasses.parallelStream()
            .filter { it.isAnnotationPresent(PlatformImplementation::class.java) }
            .filter { it.getAnnotation(PlatformImplementation::class.java).platform == TabooLib.runningPlatform() }
            .map { it.interfaces }
            .filter { it.isNotEmpty() }
            .forEach { awokenMap[it[0].name] = it[0].findInstance(true) }
    }
}
