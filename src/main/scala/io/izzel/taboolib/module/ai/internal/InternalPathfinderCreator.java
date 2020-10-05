package io.izzel.taboolib.module.ai.internal;

import io.izzel.taboolib.module.ai.PathfinderCreator;
import io.izzel.taboolib.module.ai.SimpleAi;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @Author sky
 * @Since 2018-09-19 22:31
 */
public class InternalPathfinderCreator extends net.minecraft.server.v1_8_R3.PathfinderGoal implements PathfinderCreator {

    private SimpleAi simpleAI;

    public InternalPathfinderCreator() {
    }

    public InternalPathfinderCreator(SimpleAi ai) {
        this.simpleAI = ai;
    }

    @Override
    public Object createPathfinderGoal(SimpleAi ai) {
        return new InternalPathfinderCreator(ai);
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
