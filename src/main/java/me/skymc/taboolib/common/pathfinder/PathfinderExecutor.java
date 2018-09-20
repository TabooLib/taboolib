package me.skymc.taboolib.common.pathfinder;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * @Author sky
 * @Since 2018-09-20 20:47
 */
public abstract class PathfinderExecutor {

    public abstract Object getEntityInsentient(LivingEntity entity);

    public abstract Object getNavigation(LivingEntity entity);

    public abstract Object getGoalSelector(LivingEntity entity);

    public abstract Object getTargetSelector(LivingEntity entity);

    public abstract void setGoalAi(LivingEntity entity, Object ai, int priority);

    public abstract void setTargetAi(LivingEntity entity, Object ai, int priority);

    public abstract void clearGoalAi(LivingEntity entity);

    public abstract void clearTargetAi(LivingEntity entity);

    public abstract Object navigationMove(LivingEntity entity, Location location);

    public abstract Object navigationMove(LivingEntity entity, Location location, double speed);

    public abstract boolean navigationReach(LivingEntity entity);
}
