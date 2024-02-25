package taboolib.common.reflect

/**
 * 获取指定注解的注解实例
 * 如果不存在则返回 null
 */
fun <T : Annotation> Class<*>.getAnnotationIfPresent(annotationClass: Class<T>): T? {
    return try {
        getAnnotation(annotationClass) as T
    } catch (ex: Throwable) {
        null
    }
}