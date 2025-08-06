package taboolib.module.ai;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @author sky
 * @since 2018-09-19 22:31
 */
public class PathfinderCreatorImpl17 extends net.minecraft.world.entity.ai.goal.PathfinderGoal implements PathfinderCreator {

    private SimpleAi simpleAI;

    public PathfinderCreatorImpl17() {
    }

    public PathfinderCreatorImpl17(SimpleAi ai) {
        this.simpleAI = ai;
    }

    @Override
    public Object createPathfinderGoal(SimpleAi ai) {
        return new PathfinderCreatorImpl17(ai);
    }

    @Override
    public boolean a() {
        return simpleAI.shouldExecute();
    }

    @Override
    public boolean canUse() {
        return simpleAI.shouldExecute();
    }

    @Override
    public boolean b() {
        return simpleAI.continueExecute();
    }

    @Override
    public boolean canContinueToUse() {
        return simpleAI.continueExecute();
    }

    @Override
    public void c() {
        simpleAI.startTask();
    }

    @Override
    public void start() {
        simpleAI.startTask();
    }

    @Override
    public void d() {
        simpleAI.resetTask();
    }

    @Override
    public void stop() {
        simpleAI.resetTask();
    }

    @Override
    public void e() {
        simpleAI.updateTask();
    }

    @Override
    public void tick() {
        simpleAI.updateTask();
    }
}
