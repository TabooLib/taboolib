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
@RuntimeDependencies(
    RuntimeDependency(group = "org.ow2.asm", id = "asm", version = "9.1", hash = "a99500cf6eea30535eeac6be73899d048f8d12a8"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-util", version = "9.1", hash = "36464a45d871779f3383a8a9aba2b26562a86729"),
    RuntimeDependency(group = "org.ow2.asm", id = "asm-commons", version = "9.1", hash = "8b971b182eb5cf100b9e8d4119152d83e00e0fdd")
)
@RuntimeName(group = "org.ow2.asm", name = "asm (9.1)")
@RuntimeTest(group = "org.ow2.asm", path = ["org.objectweb.asm.ClassVisitor"])
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