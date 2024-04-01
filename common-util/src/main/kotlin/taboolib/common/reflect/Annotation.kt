package taboolib.common.reflect

import java.lang.reflect.AnnotatedElement

/**
 * 获取指定注解的注解实例
 * 如果不存在则返回 null
 */
fun <T : Annotation> AnnotatedElement.getAnnotationIfPresent(annotationClass: Class<T>): T? {
    return try {
        getAnnotation(annotationClass) as T
    } catch (ex: Throwable) {
        null
    }
}

/**
 * 安全的判断是否存在指定注解
 * java.lang.ArrayStoreException: sun.reflect.annotation.AnnotationTypeMismatchExceptionProxy
 */
fun <T : Annotation> AnnotatedElement.hasAnnotation(annotationClass: Class<T>): Boolean {
    return try {
        isAnnotationPresent(annotationClass)
    } catch (ex: Throwable) {
        false
    }
}