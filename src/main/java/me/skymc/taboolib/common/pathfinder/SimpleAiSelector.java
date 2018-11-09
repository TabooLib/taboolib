package me.skymc.taboolib.common.pathfinder;

import me.skymc.taboolib.common.loader.Instantiable;
import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;

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
            internalPathfinderCreator = (PathfinderCreator) SimpleVersionControl.createNMS("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderCreator").translate().newInstance();
            internalPathfinderExecutor = (PathfinderExecutor) SimpleVersionControl.createNMS("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderExecutor").translate().newInstance();
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
