package taboolib.module.dependency

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class RuntimeDependencies(vararg val value: RuntimeDependency)