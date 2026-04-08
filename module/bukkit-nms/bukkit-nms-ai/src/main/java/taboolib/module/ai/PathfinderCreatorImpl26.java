package taboolib.module.ai;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @author sky
 * @since 2018-09-19 22:31
 */
public class PathfinderCreatorImpl26 extends net.minecraft.world.entity.ai.goal.Goal implements PathfinderCreator {

    private SimpleAi simpleAI;

    public PathfinderCreatorImpl26() {
    }

    public PathfinderCreatorImpl26(SimpleAi ai) {
        this.simpleAI = ai;
    }

    @Override
    public Object createPathfinderGoal(SimpleAi ai) {
        return new PathfinderCreatorImpl26(ai);
    }

    @Override
    public boolean canUse() {
        return simpleAI.shouldExecute();
    }

    @Override
    public boolean canContinueToUse() {
        return simpleAI.continueExecute();
    }

    @Override
    public void start() {
        simpleAI.startTask();
    }

    @Override
    public void stop() {
        simpleAI.resetTask();
    }

    @Override
    public void tick() {
        simpleAI.updateTask();
    }
}
