package me.skymc.taboolib.common.pathfinder.internal;

import me.skymc.taboolib.common.pathfinder.SimpleAi;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @Author sky
 * @Since 2018-09-19 22:31
 */
public class InternalPathfinderGoal extends net.minecraft.server.v1_12_R1.PathfinderGoal {

    private final SimpleAi simpleAI;

    public InternalPathfinderGoal(SimpleAi simpleAI) {
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
