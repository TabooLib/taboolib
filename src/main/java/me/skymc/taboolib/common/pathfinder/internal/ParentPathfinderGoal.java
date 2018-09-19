package me.skymc.taboolib.common.pathfinder.internal;

import me.skymc.taboolib.common.pathfinder.SimpleAI;

/**
 * @Author sky
 * @Since 2018-09-19 22:31
 */
public class ParentPathfinderGoal extends net.minecraft.server.v1_12_R1.PathfinderGoal {

    private final SimpleAI simpleAI;

    public ParentPathfinderGoal(SimpleAI simpleAI) {
        this.simpleAI = simpleAI;
    }

    @Override
    public boolean a() {
        return simpleAI.shouldExecute();
    }

    @Override
    public boolean b() {
        return simpleAI.continueExecute();
    }

    @Override
    public void c() {
        simpleAI.startTask();
    }

    @Override
    public void d() {
        simpleAI.resetTask();
    }

    @Override
    public void e() {
        simpleAI.updateTask();
    }
}
