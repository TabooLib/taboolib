package io.izzel.taboolib.module.ai;

/**
 * 实体 AI 实现接口
 *
 * @author sky
 * @since 2018-09-19 19:42
 */
public abstract class SimpleAi {

    /**
     * @return 是否开始执行
     */
    public abstract boolean shouldExecute();

    /**
     * @return 是否继续执行
     */
    public boolean continueExecute() {
        return shouldExecute();
    }

    /**
     * 开始时
     */
    public void startTask() {
    }

    /**
     * 重置时
     */
    public void resetTask() {
    }

    /**
     * 更新时
     */
    public void updateTask() {
    }
}
