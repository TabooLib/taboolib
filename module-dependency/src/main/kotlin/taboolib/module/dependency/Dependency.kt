package taboolib.module.dependency

import dev.vankka.dependencydownload.dependency.StandardDependency
import taboolib.common.env.KotlinEnv
import taboolib.common.env.RuntimeEnv
import taboolib.common.io.classes
import taboolib.common.platform.Awake

/**
 * TabooLib
 * taboolib.module.dependency.Dependency
 *
 * @author sky
 * @since 2021/6/15 11:57 下午
 */
@Awake
object Dependency {

    private val names = HashMap<String, String>()
    private val tests = HashMap<String, Array<String>>()
    private val dependencies = HashMap<StandardDependency, String>()

    init {
        if (KotlinEnv.isKotlinEnvironment()) {
            classes.forEach {
                if (it.isAnnotationPresent(RuntimeNames::class.java)) {
                    names.putAll(it.getAnnotation(RuntimeNames::class.java).value.map { i -> i.group to i.name })
                } else {
                    names.putAll(it.getAnnotationsByType(RuntimeName::class.java).map { i -> i.group to i.name })
                }
                if (it.isAnnotationPresent(RuntimeTests::class.java)) {
                    tests.putAll(it.getAnnotation(RuntimeTests::class.java).value.map { i -> i.group to i.path })
                } else {
                    tests.putAll(it.getAnnotationsByType(RuntimeTest::class.java).map { i -> i.group to i.path })
                }
                if (it.isAnnotationPresent(RuntimeDependencies::class.java)) {
                    dependencies.putAll(it.getAnnotation(RuntimeDependencies::class.java).value.map { i ->
                        StandardDependency(i.group, i.id, i.version, i.hash, "sha-1") to i.repository
                    })
                } else {
                    dependencies.putAll(it.getAnnotationsByType(RuntimeDependency::class.java).map { i ->
                        StandardDependency(i.group, i.id, i.version, i.hash, "sha-1") to i.repository
                    })
                }
            }
            dependencies.entries.groupBy { it.key.groupId }.forEach { (k, v) ->
                val runtimeEnv = RuntimeEnv.setup(names[k] ?: k).check(tests[k])
                val repository = HashSet<String>()
                v.forEach { (k, v) ->
                    runtimeEnv.add(k.groupId, k.artifactId, k.version, k.hash, "sha-1")
                    repository.add(v)
                }
                runtimeEnv.repository(*repository.toTypedArray()).run()
            }
        }
    }
}