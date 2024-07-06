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
        if (JavaAnnotation.hasAnnotation(clazz, Ghost.class)) {
            return;
        }
        // 检查 SkipTo
        if (lifeCycle != null && JavaAnnotation.hasAnnotation(clazz, SkipTo.class)) {
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
     * <p>
     * TODO 此方法首次运行会耗费较长时间
     */
    public static Set<Class<?>> getClasses() {
        if (classes == null) {
            HashSet<Class<?>> cache = new LinkedHashSet<>();
            long time = System.currentTimeMillis();
            // 获取所有类
            ProjectScannerKt.getRunningClassMap().entrySet().parallelStream().forEach(entry -> {
                String key = entry.getKey();
                // 只扫自己
                if (key.startsWith(ProjectIdKt.getGroupId()) || key.startsWith(ProjectIdKt.getTaboolibId())) {
                    // 排除第三方库
                    // 包名中含有 "library" 或 "libs" 不会被扫描
                    if (key.contains(".library.") || key.contains(".libs.")) {
                        return;
                    }
                    // 排除匿名内部类
                    if (isAnonymousInnerClass(key)) {
                        return;
                    }
                    // 属于 TabooLib 的类
                    if (key.startsWith(ProjectIdKt.getTaboolibPath()) || key.startsWith(ProjectIdKt.getTaboolibId())) {
                        // 没有 Inject 注解的类不会被扫描
                        if (!JavaAnnotation.hasAnnotation(entry.getValue(), Inject.class)) {
                            return;
                        }
                    }
                    // 排除其他平台
                    if (!checkPlatform(entry.getValue())) {
                        return;
                    }
                    cache.add(entry.getValue());
                }
            });
            classes = cache;
            PrimitiveIO.debug("ClassVisitor loaded %s classes. (%sms)", classes.size(), System.currentTimeMillis() - time);
        }
        return classes;
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
}
