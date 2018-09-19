package me.skymc.taboolib.common.pathfinder;

import com.ilummc.tlib.util.asm.AsmClassLoader;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.pathfinder.internal.ParentPathfinderGoalAsm;
import me.skymc.taboolib.nms.NMSUtils;
import me.skymc.taboolib.object.Instantiable;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Author sky
 * @Since 2018-09-19 20:31
 */
@Instantiable("SimpleAISelector")
public class SimpleAISelector {

    private static Class<?> parentPathfinderGoal;
    private static Class<?> pathfinderGoalFollowOwner;
    private static Class<?> pathfinderGoalSelector;
    private static Class<?> pathfinderGoal;
    private static Class<?> entityInsentient;
    private static Field targetSelector;
    private static Field goalSelector;
    private static Field navigation;
    private static Method a;

    public SimpleAISelector() {
        try {
            parentPathfinderGoal = AsmClassLoader.createNewClass("me.skymc.taboolib.common.pathfinder.internal.ParentPathfinderGoal", ParentPathfinderGoalAsm.create(TabooLib.getVersion()));
            pathfinderGoalFollowOwner = NMSUtils.getNMSClass("PathfinderGoalFollowOwner");
            pathfinderGoalSelector = NMSUtils.getNMSClass("PathfinderGoalSelector");
            pathfinderGoal = NMSUtils.getNMSClass("PathfinderGoal");
            entityInsentient = NMSUtils.getNMSClass("EntityInsentient");
            targetSelector = entityInsentient.getField("targetSelector");
            goalSelector = entityInsentient.getField("goalSelector");
            navigation = entityInsentient.getField("navigation");
            a = pathfinderGoalSelector.getDeclaredMethod("a", Integer.TYPE, pathfinderGoal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getNavigation(LivingEntity entity) {
        try {
            return navigation.get(entity.getClass().getMethod("getHandle").invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getGoalSelector(LivingEntity entity) {
        try {
            return goalSelector.get(entity.getClass().getMethod("getHandle").invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getTargetSelector(LivingEntity entity) {
        try {
            return targetSelector.get(entity.getClass().getMethod("getHandle").invoke(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setGoalAI(LivingEntity entity, Object ai, int priority) {
        try {
            a.invoke(getGoalSelector(entity), priority, ai);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setTargetAI(LivingEntity entity, Object ai, int priority) {
        try {
            a.invoke(getTargetSelector(entity), priority, ai);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
