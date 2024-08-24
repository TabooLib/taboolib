package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;

/**
 * TabooLib
 * taboolib.common.inject.ClassVisitor
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public abstract class ClassVisitor {

    private final byte priority;

    public ClassVisitor() {
        this.priority = 0;
    }

    public ClassVisitor(byte priority) {
        this.priority = priority;
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    @NotNull
    abstract public LifeCycle getLifeCycle();

    /**
     * 当类开始加载时
     *
     * @param clazz 类
     */
    public void visitStart(@NotNull ReflexClass clazz) {
    }

    /**
     * 当类结束加载时
     *
     * @param clazz 类
     */
    public void visitEnd(@NotNull ReflexClass clazz) {
    }

    /**
     * 当字段加载时
     *
     * @param field 字段
     * @param owner 所属类
     */
    public void visit(@NotNull ClassField field, @NotNull ReflexClass owner) {
    }

    /**
     * 当方法加载时
     *
     * @param method 方法
     * @param owner  所属类
     */
    public void visit(@NotNull ClassMethod method, @NotNull ReflexClass owner) {
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    public byte getPriority() {
        return this.priority;
    }

    /**
     * 查找 ReflexClass 的实例
     * 1. 从 TabooLib.getAwakenedClasses() 中查找
     * 2. 从 Kotlin 伴生类和单例类中查找
     */
    public static @Nullable Object findInstance(@NotNull ReflexClass rClass) {
        Object instance = TabooLib.getAwakenedClasses().get(rClass.getName());
        if (instance != null) return instance;
        return rClass.getInstance();
    }
}
