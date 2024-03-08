package taboolib.platform;

/**
 * TabooLib
 * taboolib.platform.Folia
 *
 * @author 坏黑
 * @since 2024/3/9 02:23
 */
public class Folia {

    public static boolean isFolia = false;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            isFolia = true;
        } catch (Throwable ignored) {
        }
    }
}
