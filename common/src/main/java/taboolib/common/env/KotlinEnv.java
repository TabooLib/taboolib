package taboolib.common.env;

@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.KotlinVersion",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk7:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.jdk7.AutoCloseableKt",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk8:@kotlin_version@",
        test = "!taboolib.library.kotlin_@kotlin_version_escape@.collections.jdk8.CollectionsJDK8Kt",
        relocate = {"!kotlin", "!taboolib.library.kotlin_@kotlin_version_escape@"}
)
public class KotlinEnv {
}