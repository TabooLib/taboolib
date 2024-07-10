package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.Inject;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.io.ProjectIdKt;
import taboolib.common.io.ProjectScannerKt;
import taboolib.common.platform.Ghost;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.SkipTo;
import taboolib.common.util.JavaAnnotation;

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
public class ClassVisitorHandler {

    private static final NavigableMap<Byte, VisitorGroup> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());
    private static Set<Class<?>> classes = null;

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
        PrimitiveIO.dev("ClassVisitorHandler initialized.");
    }

    /**
     * 检查指定类是否允许在当前平台运行
     */
    public static boolean checkPlatform(Class<?> cls) {
        PlatformSide platformSide = JavaAnnotation.getAnnotationIfPresent(cls, PlatformSide.class);
        if (platformSide == null) return true;
        for (Platform platform : platformSide.value()) {
            if (platform == Platform.CURRENT) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取能够被 ClassVisitor 访问到的所有类
     */
    public static Set<Class<?>> getClasses() {
        if (classes == null) {
            HashSet<Class<?>> cache = new LinkedHashSet<>();
            long time = System.currentTimeMillis();
            // 获取所有类
            ProjectScannerKt.getRunningClassMap().entrySet().parallelStream().forEach(entry -> {
                String key = entry.getKey();
                // 有效注入判定
                if (ProjectScannerKt.getClassMarkers().match("class-collect", key, () -> {
                    // 只扫自己
                    if (isProjectClass(key)) {
                        // 排除第三方库
                        if (isLibraryClass(key)) {
                            return false;
                        }
                        // 排除匿名内部类
                        if (isAnonymousInnerClass(key)) {
                            return false;
                        }
                        // 排除属于 TabooLib 但没有 Inject 注解的类
                        if (isTabooLibClass(key) && !JavaAnnotation.hasAnnotation(entry.getValue(), Inject.class)) {
                            return false;
                        }
                        // 最后检测有效平台
                        return checkPlatform(entry.getValue());
                    }
                    return false;
                })) {
                    cache.add(entry.getValue());
                }
            });
            classes = cache;
            PrimitiveIO.debug("ClassVisitor loaded %s classes. (%sms)", classes.size(), System.currentTimeMillis() - time);
        }
        return classes;
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
        if (ProjectScannerKt.getClassMarkers().match("inject-" + lifeCycle, clazz.getName(), () -> {
            // 跳过注入
            if (JavaAnnotation.hasAnnotation(clazz, Ghost.class)) {
                return false;
            }
            // 检查 SkipTo
            if (lifeCycle != null && JavaAnnotation.hasAnnotation(clazz, SkipTo.class)) {
                int skip = clazz.getAnnotation(SkipTo.class).value().ordinal();
                return skip <= lifeCycle.ordinal();
            }
            return true;
        })) {
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
    }

    static void visitStart(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitStart(clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, lifeCycle, ex).printStackTrace();
            }
        }
    }

    static void visitField(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
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

    static void visitMethod(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
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

    static void visitEnd(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitEnd(clazz, instance);
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
        return name.startsWith(ProjectIdKt.getGroupId()) || name.startsWith(ProjectIdKt.getTaboolibId());
    }

    /**
     * 是否为 TabooLib 类
     */
    static boolean isTabooLibClass(String name) {
        return name.startsWith(ProjectIdKt.getTaboolibPath()) || name.startsWith(ProjectIdKt.getTaboolibId());
    }

    /**
     * 是否为可能的第三方库
     * 通过包名判断
     */
    static boolean isLibraryClass(String name) {
        return name.contains(".library.") || name.contains(".libs.");
    }
}
