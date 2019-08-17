package io.izzel.taboolib.module.ai;

import io.izzel.taboolib.module.inject.TInject;

/**
 * @Author sky
 * @Since 2018-09-19 20:31
 */
public class SimpleAiSelector {

    @TInject(asm = "io.izzel.taboolib.module.ai.internal.InternalPathfinderCreator")
    private static PathfinderCreator internalPathfinderCreator;
    @TInject(asm = "io.izzel.taboolib.module.ai.internal.InternalPathfinderExecutor")
    private static PathfinderExecutor internalPathfinderExecutor;

    public static PathfinderExecutor getExecutor() {
        return internalPathfinderExecutor;
    }

    public static PathfinderCreator getCreator() {
        return internalPathfinderCreator;
    }
}
