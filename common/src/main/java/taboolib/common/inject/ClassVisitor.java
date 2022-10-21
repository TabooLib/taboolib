package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import taboolib.common.LifeCycle;

import java.util.function.Supplier;

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
     * @param clazz    类
     * @param instance 实例
     */
    public void visitStart(@NotNull Class<?> clazz, @Nullable Supplier<?> instance) {
    }

    /**
     * 当类结束加载时
     *
     * @param clazz    类
     * @param instance 实例
     */
    public void visitEnd(@NotNull Class<?> clazz, @Nullable Supplier<?> instance) {
    }

    /**
     * 当字段加载时
     *
     * @param field    字段
     * @param clazz    类
     * @param instance 实例
     */
    public void visit(@NotNull ClassField field, @NotNull Class<?> clazz, @Nullable Supplier<?> instance) {
    }

    /**
     * 当方法加载时
     *
     * @param method   方法
     * @param clazz    类
     * @param instance 实例
     */
    public void visit(@NotNull ClassMethod method, @NotNull Class<?> clazz, @Nullable Supplier<?> instance) {
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    public byte getPriority() {
        return this.priority;
    }
}
