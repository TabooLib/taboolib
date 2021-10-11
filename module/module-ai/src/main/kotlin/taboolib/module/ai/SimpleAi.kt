package taboolib.module.ai

/**
 * 实体 AI 实现接口
 *
 * @author sky
 * @since 2018-09-19 19:42
 */
abstract class SimpleAi {

    /**
     * @return 是否开始执行
     */
    abstract fun shouldExecute(): Boolean

    /**
     * @return 是否继续执行
     */
    open fun continueExecute(): Boolean {
        return shouldExecute()
    }

    /**
     * 开始时
     */
    open fun startTask() {
    }

    /**
     * 重置时
     */
    open fun resetTask() {
    }

    /**
     * 更新时
     */
    open fun updateTask() {
    }
}