package taboolib.common.env;

@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib:@kotlin_version@",
        test = "!kotlin.KotlinVersion",
        initiative = true
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk7:@kotlin_version@",
        test = "!kotlin.jdk7.AutoCloseableKt",
        initiative = true
)
@RuntimeDependency(
        value = "!org.jetbrains.kotlin:kotlin-stdlib-jdk8:@kotlin_version@",
        test = "!kotlin.collections.jdk8.CollectionsJDK8Kt",
        initiative = true
)
//@RuntimeDependency(
//        value = "!org.jetbrains.kotlin:kotlin-reflect:@kotlin_version@",
//        test = "!kotlin.reflect.full.KClassesKt",
//        initiative = true
//)
public class KotlinEnvNoRelocate { }