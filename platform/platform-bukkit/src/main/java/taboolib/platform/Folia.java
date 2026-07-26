package taboolib.platform;

import org.jetbrains.annotations.ApiStatus;

/**
 * TabooLib
 * taboolib.platform.Folia
 *
 * @author 坏黑
 * @since 2024/3/9 02:23
 */
public class Folia {

    /**
     * 当前服务端是否为 Folia。
     * <p>
     * 该值在类初始化时探测一次即固定，<b>不应由外部写入</b>——
     * 它决定了调度器的选择路径，运行期修改会让已提交的任务与新任务落在不同的调度体系上。
     * 保留为可写字段仅为兼容既有的公开访问形式，以及供测试通过 {@link #setFolia(boolean)} 切换环境。
     * <p>
     * 声明为 volatile 以保证测试或初始化过程中的写入对其他线程可见。
     */
    public static volatile boolean isFolia = false;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (Throwable ignored) {
        }
    }

    /**
     * 仅供测试切换运行环境使用，生产代码不应调用。
     */
    @ApiStatus.Internal
    public static void setFolia(boolean value) {
        isFolia = value;
    }
}
