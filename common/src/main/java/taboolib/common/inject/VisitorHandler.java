package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Ghost;
import taboolib.common.platform.PlatformFactory;
import taboolib.common.platform.SkipTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.inject.VisitorHandler
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public class VisitorHandler {

    private static final TreeMap<Byte, VisitorGroup> propertyMap = new TreeMap<>();
    private static final List<Class<?>> classes = new ArrayList<>();

    /**
     * 注册依赖注入接口
     */
    public static void register(@NotNull ClassVisitor transform) {
        VisitorGroup injectors = propertyMap.computeIfAbsent(transform.getPriority(), i -> new VisitorGroup(transform.getPriority()));
        injectors.getAll().add(transform);
    }

    /**
     * 对给定类进行依赖注入
     */
    public static void injectAll(@NotNull Class<?> clazz) {
        for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
            inject(clazz, entry.getValue(), null);
        }
    }

    /**
     * 根据生命周期对所有类进行依赖注入
     */
    public static void injectAll(@NotNull LifeCycle lifeCycle) {
        if (TabooLibCommon.isKotlinEnvironment() && !TabooLibCommon.isStopped()) {
            for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
                for (Class<?> clazz : getClasses()) {
                    inject(clazz, entry.getValue(), lifeCycle);
                }
            }
        }
    }

    /**
     * 对给定类进行依赖注入
     */
    public static void inject(@NotNull Class<?> clazz, @NotNull VisitorGroup group, @Nullable LifeCycle lifeCycle) {
        if (TabooLibCommon.isStopped()) {
            return;
        }
        // 跳过注入
        if (clazz.isAnnotationPresent(Ghost.class)) {
            return;
        }
        // 检查 SkipTo
        if (lifeCycle != null && clazz.isAnnotationPresent(SkipTo.class)) {
            int skip = clazz.getAnnotation(SkipTo.class).value().ordinal();
            if (skip > lifeCycle.ordinal()) {
                return;
            }
        }
        // 获取实例
        Supplier<?> instance = Project1Kt.getInstance(clazz, false);
        // 获取结构
        ReflexClass reflexClass;
        try {
            reflexClass = ReflexClass.Companion.of(clazz, true);
        } catch (Throwable ex) {
            new ClassVisitException(clazz, ex).printStackTrace();
            return;
        }
        // 依赖注入
        visitStart(clazz, group, lifeCycle, reflexClass, instance);
        visitField(clazz, group, lifeCycle, reflexClass, instance);
        visitMethod(clazz, group, lifeCycle, reflexClass, instance);
        visitEnd(clazz, group, lifeCycle, reflexClass, instance);
    }

    private static void visitStart(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitStart(clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, lifeCycle, ex).printStackTrace();
            }
        }
    }

    private static void visitField(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassField field : reflexClass.getStructure().getFields()) {
                try {
                    visitor.visit(field, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, lifeCycle, field, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitMethod(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassMethod method : reflexClass.getStructure().getMethods()) {
                try {
                    visitor.visit(method, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, lifeCycle, method, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitEnd(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitEnd(clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, lifeCycle, ex).printStackTrace();
            }
        }
    }

    private static List<Class<?>> getClasses() {
        if (classes.isEmpty()) {
            // 获取所有类
            for (Class<?> runningClass : Project1Kt.getRunningClasses()) {
                // 检查平台是否支持
                if (PlatformFactory.INSTANCE.checkPlatform(runningClass)) {
                    classes.add(runningClass);
                }
            }
        }
        return classes;
    }
}
