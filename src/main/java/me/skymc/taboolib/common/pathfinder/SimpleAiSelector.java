package me.skymc.taboolib.common.pathfinder;

import com.ilummc.tlib.util.asm.AsmClassLoader;
import me.skymc.taboolib.common.pathfinder.generate.PathfinderExecutorGenerator;
import me.skymc.taboolib.common.pathfinder.generate.PathfinderGoalGenerator;
import me.skymc.taboolib.object.Instantiable;

/**
 * @Author sky
 * @Since 2018-09-19 20:31
 */
@Instantiable("SimpleAISelector")
public class SimpleAiSelector {

    private static Class<?> internalPathfinderGoal;
    private static PathfinderExecutor internalPathfinderExecutor;

    public SimpleAiSelector() {
        try {
            internalPathfinderGoal = AsmClassLoader.createNewClass("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderGoal", PathfinderGoalGenerator.generate());
            internalPathfinderExecutor = (PathfinderExecutor) AsmClassLoader.createNewClass("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderExecutor", PathfinderExecutorGenerator.generate()).newInstance();
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
}
