package taboolib.common.classloader;

/**
 * TabooLib
 * taboolib.common.classloader.Precondition
 *
 * @author 坏黑
 * @since 2024/1/25 22:09
 */
public class Precondition {

    public static void onlyIsolated(Class<?> cls) {
        if (!(cls.getClassLoader() instanceof IsolatedClassLoader)) {
            throw new IllegalStateException("Illegal call.");
        }
    }

    public static void notIsolated(Class<?> cls) {
        if (cls.getClassLoader() instanceof IsolatedClassLoader) {
            throw new IllegalStateException("Illegal call.");
        }
    }
}
