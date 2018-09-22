package me.skymc.taboolib.common.pathfinder;

import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;
import me.skymc.taboolib.object.Instantiable;

/**
 * @Author sky
 * @Since 2018-09-19 20:31
 */
@Instantiable("SimpleAiSelector")
public class SimpleAiSelector {

    private static PathfinderCreator internalPathfinderCreator;
    private static PathfinderExecutor internalPathfinderExecutor;

    public SimpleAiSelector() {
        try {
            internalPathfinderCreator = (PathfinderCreator) SimpleVersionControl.create()
                    .from("1_8_R3")
                    .target("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderCreator")
                    .translate()
                    .newInstance();
            internalPathfinderExecutor = (PathfinderExecutor) SimpleVersionControl.create()
                    .from("1_8_R3")
                    .target("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderExecutor")
                    .translate()
                    .newInstance();
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
