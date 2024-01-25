package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.io.ProjectIdKt;
import taboolib.common.io.ProjectScannerKt;
import taboolib.common.platform.Ghost;
import taboolib.common.platform.Platform;
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
    private static final List<Class<?>> classes = new ArrayList<>();

    static void init() {
        for (LifeCycle lifeCycle : LifeCycle.values()) {
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
        long time = System.currentTimeMillis();
        if (TabooLib.isKotlinEnvironment() && !TabooLib.isStopped()) {
            for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
                for (Class<?> clazz : getClasses()) {
                    inject(clazz, entry.getValue(), lifeCycle);
                }
            }
        }
        PrimitiveIO.println("ClassVisitor inject %s in %sms.", lifeCycle, System.currentTimeMillis() - time);
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz     类
     * @param group     注入组
     * @param lifeCycle 生命周期
     */
    public static void inject(@NotNull Class<?> clazz, @NotNull VisitorGroup group, @Nullable LifeCycle lifeCycle) {
        if (TabooLib.isStopped()) {
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
        Supplier<?> instance = ProjectScannerKt.getInstance(clazz, false);
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

    public static List<Class<?>> getClasses() {
        if (classes.isEmpty()) {
            // 获取所有类
            for (Map.Entry<String, Class<?>> it : ProjectScannerKt.getRunningClassMap().entrySet()) {
                // 排除第三方库
                // 位于 com.example.plugin.library.* 或 com.example.plugin.taboolib.library.* 下的包不会被检查
                if (!it.getKey().startsWith(ProjectIdKt.getGroupId() + ".library") && !it.getKey().startsWith(ProjectIdKt.getTaboolibPath() + ".library")) {
                    // 排除其他平台
                    if (Platform.check(it.getValue())) {
                        classes.add(it.getValue());
                    }
                }
            }
        }
        return classes;
    }
}
