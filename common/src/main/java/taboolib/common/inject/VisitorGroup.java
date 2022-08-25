package taboolib.common.inject;

import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TabooLib
 * taboolib.common.inject.VisitorGroup
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public class VisitorGroup {

    private final byte priority;
    private final List<ClassVisitor> list = new CopyOnWriteArrayList<>();

    public VisitorGroup(byte priority) {
        this.priority = priority;
    }

    /**
     * 获取所有
     */
    public List<ClassVisitor> getAll() {
        return list;
    }

    /**
     * 通过生命周期获取
     */
    public List<ClassVisitor> get(@Nullable LifeCycle lifeCycle) {
        List<ClassVisitor> classList = new LinkedList<>();
        for (ClassVisitor inj : list) {
            if (lifeCycle == null || lifeCycle == inj.getLifeCycle()) {
                classList.add(inj);
            }
        }
        return classList;
    }

    public byte getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "VisitorGroup{" +
                "priority=" + priority +
                ", list=" + list +
                '}';
    }
}