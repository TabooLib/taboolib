package taboolib.module.dependency

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Repeatable
annotation class RuntimeDependency(
    val group: String,
    val id: String,
    val version: String,
    val hash: String,
    val repository: String = "https://maven.aliyun.com/repository/central"
)