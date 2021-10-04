package taboolib.common.env;

@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib:@kotlin_version@",
        test = "!kotlin.KotlinVersion"
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk7:@kotlin_version@",
        test = "!kotlin.jdk7.AutoCloseableKt"
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk8:@kotlin_version@",
        test = "!kotlin.collections.jdk8.CollectionsJDK8Kt"
)
public class KotlinEnvNoRelocate { }