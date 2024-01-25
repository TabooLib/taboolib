package taboolib.common;

/**
 * TabooLib
 * taboolib.common.LifeCycleTask
 *
 * @author 坏黑
 * @since 2024/1/26 02:01
 */
public interface LifeCycleTask {

    /**
     * 优先级（生序排列）
     */
    int priority();

    /**
     * 运行
     */
    void run();
}
