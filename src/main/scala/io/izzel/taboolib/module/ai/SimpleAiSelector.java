package io.izzel.taboolib.module.ai;

import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.lite.SimpleVersionControl;

/**
 * @Author sky
 * @Since 2018-09-19 20:31
 */
@TFunction(enable = "init")
public class SimpleAiSelector {

    private static PathfinderCreator internalPathfinderCreator;
    private static PathfinderExecutor internalPathfinderExecutor;

    static void init() {
        try {
            internalPathfinderCreator = (PathfinderCreator) SimpleVersionControl.createNMS("io.izzel.taboolib.module.ai.internal.InternalPathfinderCreator").translate().newInstance();
            internalPathfinderExecutor = (PathfinderExecutor) SimpleVersionControl.createNMS("io.izzel.taboolib.module.ai.internal.InternalPathfinderExecutor").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static PathfinderExecutor getExecutor() {
        return internalPathfinderExecutor;
    }

    public static PathfinderCreator getCreator() {
        return internalPathfinderCreator;
    }
}
