package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.LifeCycle;
import taboolib.common.Inject;
import taboolib.common.TabooLib;
import taboolib.common.io.ProjectIdKt;
import taboolib.common.io.ProjectScannerKt;
import taboolib.common.platform.Ghost;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.SkipTo;

import java.util.*;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.inject.VisitorHandler
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
@SuppressWarnings("CallToPrintStackTrace")
public class VisitorHandler {

    private static final NavigableMap<Byte, VisitorGroup> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());
    private static final Set<Class<?>> classes = new HashSet<>();

    static void init() {
        for (LifeCycle lifeCycle : LifeCycle.values()) {
            if (lifeCycle == LifeCycle.NONE) {
                continue;
            }
            // 只有 CONST 生命周期下优先级为 1，因为要在 PlatformFactory 之后运行
            int priority = lifeCycle == LifeCycle.CONST ? 1 : 0;
            // 注册任务
            TabooLib.registerLifeCycleTask(lifeCycle, priority, () -> VisitorHandler.injectAll(lifeCycle));
        }
    }

    /**
     * 注册依赖注入接口
     *
     * @param classVisitor 接口
     */
    public static void register(@NotNull ClassVisitor classVisitor) {
        VisitorGroup injectors = propertyMap.computeIfAbsent(classVisitor.getPriority(), i -> new VisitorGroup(classVisitor.getPriority()));
        injectors.getAll().add(classVisitor);
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz 类
     */
    public static void injectAll(@NotNull Class<?> clazz) {
        for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
            inject(clazz, entry.getValue(), null);
        }
    }

    /**
     * 根据生命周期对所有类进行依赖注入
     *
     * @param lifeCycle 生命周期
     */
    public static void injectAll(@NotNull LifeCycle lifeCycle) {
        for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
            for (Class<?> clazz : getClasses()) {
                inject(clazz, entry.getValue(), lifeCycle);
            }
        }
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz     类
     * @param group     注入组
     * @param lifeCycle 生命周期
     */
    public static void inject(@NotNull Class<?> clazz, @NotNull VisitorGroup group, @Nullable LifeCycle lifeCycle) {
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
        Supplier<?> instance = ProjectScannerKt.getInstance(clazz, false);
        // 获取结构
        ReflexClass rc;
        try {
            rc = ReflexClass.Companion.of(clazz, true);
        } catch (Throwable ex) {
            new ClassVisitException(clazz, ex).printStackTrace();
            return;
        }
        // 依赖注入
        visitStart(clazz, group, lifeCycle, rc, instance);
        visitField(clazz, group, lifeCycle, rc, instance);
        visitMethod(clazz, group, lifeCycle, rc, instance);
        visitEnd(clazz, group, lifeCycle, rc, instance);
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

    /**
     * 获取能够被 ClassVisitor 访问到的所有类
     */
    public static Set<Class<?>> getClasses() {
        if (classes.isEmpty()) {
            // 获取所有类
            for (Map.Entry<String, Class<?>> it : ProjectScannerKt.getRunningClassMap().entrySet()) {
                // 只扫自己
                if (it.getKey().startsWith(ProjectIdKt.getGroupId())) {
                    // 排除第三方库
                    // 位于 com.example.plugin.library.* 或 com.example.plugin.taboolib.library.* 下的包不会被检查
                    if (it.getKey().startsWith(ProjectIdKt.getGroupId() + ".library") || it.getKey().startsWith(ProjectIdKt.getTaboolibPath() + ".library")) {
                        continue;
                    }
                    // 排除 TabooLib 的非开放类
                    if (it.getKey().startsWith(ProjectIdKt.getTaboolibPath()) && !it.getValue().isAnnotationPresent(Inject.class)) {
                        continue;
                    }
                    // 排除其他平台
                    if (!checkPlatform(it.getValue())) {
                        continue;
                    }
                    classes.add(it.getValue());
                }
            }
        }
        return classes;
    }

    /**
     * 检查指定类是否允许在当前平台运行
     */
    public static boolean checkPlatform(Class<?> cls) {
        PlatformSide platformSide = cls.getAnnotation(PlatformSide.class);
        return platformSide == null || Arrays.stream(platformSide.value()).anyMatch(i -> i == Platform.CURRENT);
    }
}
