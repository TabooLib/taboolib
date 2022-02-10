package taboolib.common.env;

@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib:@kotlin_version@",
        test = "!kotlin@kotlin_version_escape@.KotlinVersion",
        relocate = {"!kotlin.", "!kotlin@kotlin_version_escape@."},
        initiative = true
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk7:@kotlin_version@",
        test = "!kotlin@kotlin_version_escape@.jdk7.AutoCloseableKt",
        relocate = {"!kotlin.", "!kotlin@kotlin_version_escape@."},
        initiative = true
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk8:@kotlin_version@",
        test = "!kotlin@kotlin_version_escape@.collections.jdk8.CollectionsJDK8Kt",
        relocate = {"!kotlin.", "!kotlin@kotlin_version_escape@."},
        initiative = true
)
public class KotlinEnv { }