package taboolib.module.ai;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @author sky
 * @since 2018-09-19 22:31
 */
public class PathfinderCreatorImpl extends net.minecraft.server.v1_16_R3.PathfinderGoal implements PathfinderCreator {

    private SimpleAi simpleAI;

    public PathfinderCreatorImpl() {
    }

    public PathfinderCreatorImpl(SimpleAi ai) {
        this.simpleAI = ai;
    }

    @Override
    public Object createPathfinderGoal(SimpleAi ai) {
        return new PathfinderCreatorImpl(ai);
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
