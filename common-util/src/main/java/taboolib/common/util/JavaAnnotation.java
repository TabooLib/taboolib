package taboolib.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * TabooLib
 * taboolib.common.util.JavaAnnotation
 *
 * @author 坏黑
 * @since 2024/4/1 21:52
 */
public class JavaAnnotation {

    /**
     * 获取指定注解的注解实例
     * 如果不存在则返回 null
     */
    public static <T extends Annotation> T getAnnotationIfPresent(AnnotatedElement ae, Class<T> annotationClass) {
        try {
            return ae.getAnnotation(annotationClass);
        } catch (Throwable ex) {
            return null;
        }
    }

    /**
     * 安全的判断是否存在指定注解
     * 防止java.lang.ArrayStoreException: sun.reflect.annotation.AnnotationTypeMismatchExceptionProxy异常
     */
    public static <T extends Annotation> boolean hasAnnotation(AnnotatedElement ae, Class<T> annotationClass) {
        try {
            return ae.isAnnotationPresent(annotationClass);
        } catch (Throwable ex) {
            return false;
        }
    }
}
