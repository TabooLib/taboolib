package io.izzel.taboolib.module.ai;

import io.izzel.taboolib.module.inject.TInject;

/**
 * 实体 AI 相关工具
 *
 * @author sky
 * @since 2018-09-19 20:31
 */
public class SimpleAiSelector {

    @TInject(asm = "io.izzel.taboolib.module.ai.internal.InternalPathfinderCreator")
    private static PathfinderCreator internalPathfinderCreator;
    @TInject(asm = "io.izzel.taboolib.module.ai.internal.InternalPathfinderExecutor")
    private static PathfinderExecutor internalPathfinderExecutor;

    /**
     * 获取实体 AI 执行工具
     *
     * @return {@link PathfinderExecutor}
     */
    public static PathfinderExecutor getExecutor() {
        return internalPathfinderExecutor;
    }

    /**
     * 获取实体 AI 构建工具
     *
     * @return {@link PathfinderCreator}
     */
    public static PathfinderCreator getCreator() {
        return internalPathfinderCreator;
    }
}
