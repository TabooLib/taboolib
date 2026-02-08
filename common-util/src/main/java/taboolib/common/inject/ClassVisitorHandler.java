package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.*;
import taboolib.common.Inject;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.io.ProjectInfoKt;
import taboolib.common.io.ProjectScannerKt;
import taboolib.common.platform.Ghost;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.SkipTo;

import java.util.*;

/**
 * TabooLib
 * taboolib.common.inject.VisitorHandler
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
@SuppressWarnings("CallToPrintStackTrace")
public class ClassVisitorHandler {

    private static final NavigableMap<Byte, VisitorGroup> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());
    private static Set<ReflexClass> classes = null;

    /**
     * 初始化函数
     */
    static void init() {
        for (LifeCycle lifeCycle : LifeCycle.values()) {
            if (lifeCycle == LifeCycle.NONE) {
                continue;
            }
            // 只有 CONST 生命周期下优先级为 1，因为要在 PlatformFactory 之后运行
            int priority = lifeCycle == LifeCycle.CONST ? 1 : 0;
            // 注册任务
            TabooLib.registerLifeCycleTask(lifeCycle, priority, () -> ClassVisitorHandler.injectAll(lifeCycle));
        }
        PrimitiveIO.debug("ClassVisitorHandler 初始化完成。");
    }

    /**
     * 获取能够被 ClassVisitor 访问到的所有类
     */
    public static Set<ReflexClass> getClasses() {
        if (classes == null) {
            HashSet<ReflexClass> cache = new LinkedHashSet<>();
            long time = TabooLib.execution(() -> {
                // 获取所有类
                // 这里会首次触发 runningClassMapInJar 的初始化
                for (Map.Entry<String, ReflexClass> entry : ProjectScannerKt.getRunningClassMap().entrySet()) {
                    String key = entry.getKey();
                    ReflexClass value = entry.getValue();
                    // 排除非本项目 && 排除第三方库 && 排除匿名内部类
                    if (!isProjectClass(key) || isLibraryClass(key) || isAnonymousInnerClass(key)) {
                        continue;
                    }
                    // 排除属于 TabooLib 但没有 Inject 注解的类
                    if (isTabooLibClass(key) && !value.getStructure().isAnnotationPresent(Inject.class)) {
                        continue;
                    }
                    // 检测有效平台
                    if (checkPlatform(value)) {
                        cache.add(value);
                    }
                }
                classes = cache;
            });
            PrimitiveIO.debug("ClassVisitor 收集到 {0} 个有效类，用时 {1} 毫秒。", classes.size(), time);
        }
        return classes;
    }

    /**
     * 检查指定类是否允许在当前平台运行
     */
    public static boolean checkPlatform(ReflexClass cls) {
        if (cls.getStructure().isAnnotationPresent(PlatformSide.class)) {
            ClassAnnotation annotation = cls.getStructure().getAnnotation(PlatformSide.class);
            List<String> value = annotation.enumNameList("value");
            return value.isEmpty() || value.contains(Platform.CURRENT.name());
        }
        return true;
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
    public static void injectAll(@NotNull ReflexClass clazz) {
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
            for (ReflexClass clazz : getClasses()) {
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
    public static void inject(@NotNull ReflexClass clazz, @NotNull VisitorGroup group, @Nullable LifeCycle lifeCycle) {
        // 跳过注入
        if (clazz.getStructure().isAnnotationPresent(Ghost.class)) {
            return;
        }
        // 检查 SkipTo
        if (lifeCycle != null && clazz.getStructure().isAnnotationPresent(SkipTo.class)) {
            int skip = clazz.getStructure().getAnnotation(SkipTo.class).getEnum("value", LifeCycle.CONST).ordinal();
            if (skip > lifeCycle.ordinal()) return;
        }
        // 依赖注入
        visitStart(clazz, group, lifeCycle);
        visitField(clazz, group, lifeCycle);
        visitMethod(clazz, group, lifeCycle);
        visitEnd(clazz, group, lifeCycle);
    }

    static void visitStart(ReflexClass clazz, VisitorGroup group, LifeCycle lifeCycle) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitStart(clazz);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, lifeCycle, ex).printStackTrace();
            }
        }
    }

    static void visitField(ReflexClass clazz, VisitorGroup group, LifeCycle lifeCycle) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassField field : clazz.getStructure().getFields()) {
                try {
                    visitor.visit(field, clazz);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, lifeCycle, field, ex).printStackTrace();
                }
            }
        }
    }

    static void visitMethod(ReflexClass clazz, VisitorGroup group, LifeCycle lifeCycle) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassMethod method : clazz.getStructure().getMethods()) {
                try {
                    visitor.visit(method, clazz);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, lifeCycle, method, ex).printStackTrace();
                }
            }
        }
    }

    static void visitEnd(ReflexClass clazz, VisitorGroup group, LifeCycle lifeCycle) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitEnd(clazz);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, lifeCycle, ex).printStackTrace();
            }
        }
    }

    /**
     * 是否为匿名内部类
     */
    static boolean isAnonymousInnerClass(String name) {
        int lastSpectator = name.lastIndexOf("$");
        if (lastSpectator == -1) return false;
        String className = name.substring(lastSpectator + 1);
        for (int i = 0; i < className.length(); i++) {
            if (!Character.isDigit(className.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为本项目的类
     */
    static boolean isProjectClass(String name) {
        return name.startsWith(ProjectInfoKt.getGroupId()) || name.startsWith(ProjectInfoKt.getTaboolibId());
    }

    /**
     * 是否为 TabooLib 类
     */
    static boolean isTabooLibClass(String name) {
        return name.startsWith(ProjectInfoKt.getTaboolibPath()) || name.startsWith(ProjectInfoKt.getTaboolibId());
    }

    /**
     * 是否为可能的第三方库
     * 通过包名判断
     */
    static boolean isLibraryClass(String name) {
        return name.contains(".library.") || name.contains(".libs.");
    }
}
