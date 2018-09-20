package me.skymc.taboolib.common.pathfinder.internal;

import me.skymc.taboolib.common.pathfinder.PathfinderExecutor;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @Author sky
 * @Since 2018-09-20 20:57
 */
public class InternalPathfinderExecutor extends PathfinderExecutor {

    private Field pathEntity;
    private Field pathfinderGoalSelectorSet;

    public InternalPathfinderExecutor() {
        try {
            pathfinderGoalSelectorSet = PathfinderGoalSelector.class.getDeclaredField("b");
            pathfinderGoalSelectorSet.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Field field : NavigationAbstract.class.getDeclaredFields()) {
            if (field.getType().equals(PathEntity.class)) {
                field.setAccessible(true);
                pathEntity = field;
                return;
            }
        }
    }

    @Override
    public Object getEntityInsentient(LivingEntity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public Object getNavigation(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).getNavigation();
    }

    @Override
    public Object getGoalSelector(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).goalSelector;
    }

    @Override
    public Object getTargetSelector(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).targetSelector;
    }

    @Override
    public void setGoalAi(LivingEntity entity, Object ai, int priority) {
        ((EntityInsentient) getEntityInsentient(entity)).goalSelector.a(priority, (PathfinderGoal) ai);
    }

    @Override
    public void setTargetAi(LivingEntity entity, Object ai, int priority) {
        ((EntityInsentient) getEntityInsentient(entity)).targetSelector.a(priority, (PathfinderGoal) ai);
    }

    @Override
    public void clearGoalAi(LivingEntity entity) {
        try {
            ((Set) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).goalSelector)).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearTargetAi(LivingEntity entity) {
        try {
            ((Set) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).targetSelector)).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object navigationMove(LivingEntity entity, Location location) {
        return navigationMove(entity, location, entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());
    }

    @Override
    public Object navigationMove(LivingEntity entity, Location location, double speed) {
        return ((Navigation) getNavigation(entity)).a(location.getX(), location.getY(), location.getZ(), speed);
    }

    @Override
    public boolean navigationReach(LivingEntity entity) {
        PathEntity pathEntity = getPathEntity(entity);
        return pathEntity == null || pathEntity.b();
    }

    private PathEntity getPathEntity(LivingEntity entity) {
        try {
            return (PathEntity) pathEntity.get(getNavigation(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
