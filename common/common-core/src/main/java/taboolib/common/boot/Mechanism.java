package taboolib.common.boot;

/**
 * TabooLib
 * taboolib.common.boot.Mechanism
 *
 * @author 坏黑
 * @since 2022/1/28 4:30 PM
 */
public interface Mechanism {

    void startup();

    void shutdown();

    static void startup(Object obj) {
        if (obj instanceof Mechanism) {
            ((Mechanism) obj).startup();
        }
    }

    static void shutdown(Object obj) {
        if (obj instanceof Mechanism) {
            ((Mechanism) obj).shutdown();
        }
    }
}
