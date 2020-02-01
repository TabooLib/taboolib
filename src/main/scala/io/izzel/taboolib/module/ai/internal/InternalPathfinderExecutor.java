package io.izzel.taboolib.module.ai.internal;

import io.izzel.taboolib.module.ai.PathfinderExecutor;
import io.izzel.taboolib.module.ai.SimpleAi;
import io.izzel.taboolib.module.ai.SimpleAiSelector;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.util.Ref;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 该类仅用作生成 ASM 代码，无任何意义
 *
 * @Author sky
 * @Since 2018-09-20 20:57
 */
public class InternalPathfinderExecutor extends PathfinderExecutor {

    private Field pathEntity;
    private Field pathfinderGoalSelectorSet;
    private Field controllerJumpCurrent;

    public InternalPathfinderExecutor() {
        try {
            SimpleReflection.saveField(PathfinderGoalSelector.class);
            SimpleReflection.saveField(ControllerJump.class);
            pathfinderGoalSelectorSet =SimpleReflection.getField(PathfinderGoalSelector.class, "b");
            controllerJumpCurrent = SimpleReflection.getField(ControllerJump.class, "a");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleReflection.saveField(NavigationAbstract.class);
            for (Field field : SimpleReflection.getFields(NavigationAbstract.class).values()) {
                if (field.getType().equals(PathEntity.class)) {
                    pathEntity = field;
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public Object getControllerJump(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).getControllerJump();
    }

    @Override
    public Object getControllerMove(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).getControllerMove();
    }

    @Override
    public Object getControllerLook(LivingEntity entity) {
        return ((EntityInsentient) getEntityInsentient(entity)).getControllerLook();
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
    public Object getPathEntity(LivingEntity entity) {
        try {
            return pathEntity.get(getNavigation(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPathEntity(LivingEntity entity, Object pathEntity) {
        try {
            Ref.putField(getNavigation(entity), this.pathEntity, pathEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setGoalAi(LivingEntity entity, SimpleAi ai, int priority) {
        ((EntityInsentient) getEntityInsentient(entity)).goalSelector.a(priority, (PathfinderGoal) SimpleAiSelector.getCreator().createPathfinderGoal(ai));
    }

    @Override
    public void setTargetAi(LivingEntity entity, SimpleAi ai, int priority) {
        ((EntityInsentient) getEntityInsentient(entity)).targetSelector.a(priority, (PathfinderGoal) SimpleAiSelector.getCreator().createPathfinderGoal(ai));
    }

    @Override
    public void clearGoalAi(LivingEntity entity) {
        try {
            ((Collection) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).goalSelector)).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearTargetAi(LivingEntity entity) {
        try {
            ((Collection) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).targetSelector)).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterable getGoalAi(LivingEntity entity) {
        try {
            return ((Collection) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).goalSelector));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable getTargetAi(LivingEntity entity) {
        try {
            return ((Collection) pathfinderGoalSelectorSet.get(((EntityInsentient) getEntityInsentient(entity)).targetSelector));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public void setGoalAi(LivingEntity entity, Iterable ai) {
        try {
            Ref.putField(((EntityInsentient) getEntityInsentient(entity)).goalSelector,
                this.pathfinderGoalSelectorSet, ai);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void setTargetAi(LivingEntity entity, Iterable ai) {
        try {
            Ref.putField(((EntityInsentient) getEntityInsentient(entity)).targetSelector,
                this.pathfinderGoalSelectorSet, ai);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean navigationMove(LivingEntity entity, Location location) {
        return navigationMove(entity, location, 0.6);
    }

    @Override
    public boolean navigationMove(LivingEntity entity, Location location, double speed) {
        return ((Navigation) getNavigation(entity)).a(location.getX(), location.getY(), location.getZ(), speed);
    }

    @Override
    public boolean navigationMove(LivingEntity entity, LivingEntity target) {
        return navigationMove(entity, target, 0.6);
    }

    @Override
    public boolean navigationMove(LivingEntity entity, LivingEntity target, double speed) {
        return ((Navigation) getNavigation(entity)).a(((CraftEntity) target).getHandle(), speed);
    }

    @Override
    public boolean navigationReach(LivingEntity entity) {
        Object pathEntity = getPathEntity(entity);
        return pathEntity == null || ((PathEntity) pathEntity).b();
    }

    @Override
    public void controllerLookAt(LivingEntity entity, Location target) {
        ((ControllerLook) getControllerLook(entity)).a(target.getX(), target.getY(), target.getZ(), 10, 40);
    }

    @Override
    public void controllerLookAt(LivingEntity entity, Entity target) {
        ((ControllerLook) getControllerLook(entity)).a(((CraftEntity) target).getHandle(), 10, 40);
    }

    @Override
    public void controllerJumpReady(LivingEntity entity) {
        ((ControllerJump) getControllerJump(entity)).a();
    }

    @Override
    public boolean controllerJumpCurrent(LivingEntity entity) {
        try {
            return controllerJumpCurrent.getBoolean(getControllerJump(entity));
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void setFollowRange(LivingEntity entity, double value) {
        ((EntityInsentient) getEntityInsentient(entity)).getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(value);
    }
}
