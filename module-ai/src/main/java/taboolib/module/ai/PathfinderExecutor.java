package taboolib.module.ai;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * @author sky
 * @since 2018-09-20 20:47
 */
@SuppressWarnings("rawtypes")
public abstract class PathfinderExecutor {

    public abstract Object getEntityInsentient(LivingEntity entity);

    public abstract Object getNavigation(LivingEntity entity);

    public abstract Object getControllerJump(LivingEntity entity);

    public abstract Object getControllerMove(LivingEntity entity);

    public abstract Object getControllerLook(LivingEntity entity);

    public abstract Object getGoalSelector(LivingEntity entity);

    public abstract Object getTargetSelector(LivingEntity entity);

    public abstract Object getPathEntity(LivingEntity entity);

    public abstract void setPathEntity(LivingEntity entity, Object pathEntity);

    public abstract void addGoalAi(LivingEntity entity, SimpleAi ai, int priority);

    public abstract void addTargetAi(LivingEntity entity, SimpleAi ai, int priority);

    public abstract void replaceGoalAi(LivingEntity entity, SimpleAi ai, int priority);

    public abstract void replaceTargetAi(LivingEntity entity, SimpleAi ai, int priority);

    public abstract void replaceGoalAi(LivingEntity entity, SimpleAi ai, int priority, @Nullable String name);

    public abstract void replaceTargetAi(LivingEntity entity, SimpleAi ai, int priority, @Nullable String name);

    public abstract void removeGoalAi(LivingEntity entity, int priority);

    public abstract void removeTargetAi(LivingEntity entity, int priority);

    public abstract void removeGoalAi(LivingEntity entity, String name);

    public abstract void removeTargetAi(LivingEntity entity, String name);

    public abstract void clearGoalAi(LivingEntity entity);

    public abstract void clearTargetAi(LivingEntity entity);

    public abstract Iterable getGoalAi(LivingEntity entity);

    public abstract Iterable getTargetAi(LivingEntity entity);

    public abstract void setGoalAi(LivingEntity entity, Iterable ai);

    public abstract void setTargetAi(LivingEntity entity, Iterable ai);

    public abstract boolean navigationMove(LivingEntity entity, Location location);

    public abstract boolean navigationMove(LivingEntity entity, Location location, double speed);

    public abstract boolean navigationMove(LivingEntity entity, LivingEntity target);

    public abstract boolean navigationMove(LivingEntity entity, LivingEntity target, double speed);

    public abstract boolean navigationReach(LivingEntity entity);

    public abstract void controllerLookAt(LivingEntity entity, Location target);

    public abstract void controllerLookAt(LivingEntity entity, Entity target);

    public abstract void controllerJumpReady(LivingEntity entity);

    public abstract boolean controllerJumpCurrent(LivingEntity entity);

    public abstract void setFollowRange(LivingEntity entity, double value);
}
