package taboolib.common.env

import dev.vankka.dependencydownload.dependency.StandardDependency
import taboolib.common.env.runtime.*
import taboolib.common.io.classes
import taboolib.common.platform.PlatformInstance

/**
 * TabooLib
 * taboolib.common.env.RuntimeLoader
 *
 * @author sky
 * @since 2021/6/15 11:57 下午
 */
@PlatformInstance
object DependencyLoader {

    val names = HashMap<String, String>()
    val tests = HashMap<String, String>()
    val dependencies = HashMap<StandardDependency, String>()

    init {
        if (KotlinEnv.isKotlinEnvironment()) {
            classes.forEach {
                if (it.isAnnotationPresent(RuntimeNames::class.java)) {
                    names.putAll(it.getAnnotation(RuntimeNames::class.java).value.map { i -> i.group to i.name })
                }
                if (it.isAnnotationPresent(RuntimeTests::class.java)) {
                    tests.putAll(it.getAnnotation(RuntimeTests::class.java).value.map { i -> i.group to i.path })
                }
                if (it.isAnnotationPresent(RuntimeDependencies::class.java)) {
                    dependencies.putAll(it.getAnnotation(RuntimeDependencies::class.java).value.map { i ->
                        StandardDependency(i.group, i.id, i.version, i.hash, "sha-1") to i.repository
                    })
                }
            }
        }
    }
}