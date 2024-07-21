package taboolib.common;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TabooLib
 * taboolib.common.TabooLib
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
public class TabooLib {

    /**
     * 是否停止加载
     **/
    private static boolean isStopped = false;

    /**
     * 当前生命周期
     **/
    private static LifeCycle currentLifeCycle = LifeCycle.NONE;

    /**
     * 当前插件所有被唤醒的类
     */
    private static final ConcurrentHashMap<String, Object> awakenedClasses = new ConcurrentHashMap<>();

    /**
     * 生命周期任务
     **/
    private static final ConcurrentHashMap<LifeCycle, List<LifeCycleTask>> lifeCycleTask = new ConcurrentHashMap<>();

    /**
     * 类查找器
     */
    private static ClassFinder classFinder = new ClassFinder() {

        @Override
        public Class<?> getClass(String name) throws ClassNotFoundException {
            return Class.forName(name);
        }

        @Override
        public Class<?> getClass(String name, boolean initialize) throws ClassNotFoundException {
            return Class.forName(name, initialize, TabooLib.class.getClassLoader());
        }

        @Override
        public Class<?> getClass(String name, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
            return Class.forName(name, initialize, classLoader);
        }
    };

    /**
     * 执行生命周期任务
     */
    public static void lifeCycle(LifeCycle lifeCycle) {
        if (isStopped) {
            return;
        }
        // 检查 Kotlin 环境是否就绪
        if (!TabooLib.isKotlinEnvironment()) {
            isStopped = true;
            throw new RuntimeException("Runtime environment setup failed, please feedback! (Kotlin Environment Not Found)");
        }
        // 开发者模式下打印生命周期
        PrimitiveIO.debug("LifeCycle: " + lifeCycle);
        // 记录生命周期
        currentLifeCycle = lifeCycle;
        // 运行生命周期任务
        List<LifeCycleTask> taskList = lifeCycleTask.remove(lifeCycle);
        if (taskList != null) {
            for (LifeCycleTask task : taskList) {
                task.run();
            }
        }
    }

    /**
     * 推迟任务到指定生命周期下执行，如果生命周期已经过去则立即执行
     *
     * @param lifeCycle 生命周期
     * @param runnable  任务
     */
    public static void registerLifeCycleTask(LifeCycle lifeCycle, int priority, Runnable runnable) {
        if (TabooLib.currentLifeCycle.ordinal() >= lifeCycle.ordinal()) {
            runnable.run();
        } else {
            List<LifeCycleTask> tasks;
            if (lifeCycleTask.containsKey(lifeCycle)) {
                tasks = lifeCycleTask.get(lifeCycle);
            } else {
                tasks = new CopyOnWriteArrayList<>();
                lifeCycleTask.put(lifeCycle, tasks);
            }
            tasks.add(new LifeCycleTask() {

                @Override
                public int priority() {
                    return priority;
                }

                @Override
                public void run() {
                    runnable.run();
                }
            });
            tasks.sort(Comparator.comparingInt(LifeCycleTask::priority));
        }
    }

    /**
     * 检查当前 Kotlin 环境是否有效
     */
    public static boolean isKotlinEnvironment() {
        try {
            Class.forName("kotlin.Lazy", false, ClassAppender.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 检查当前 Kotlin Coroutines 环境是否有效
     */
    public static boolean isKotlinCoroutinesEnvironment() {
        try {
            Class.forName("kotlinx.coroutines.CoroutineScope", false, ClassAppender.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 获取当前生命周期
     */
    @NotNull
    public static LifeCycle getCurrentLifeCycle() {
        return currentLifeCycle;
    }

    /**
     * 是否停止 TabooLib 及插件加载流程
     */
    public static boolean isStopped() {
        return isStopped;
    }

    /**
     * 停止 TabooLib 及插件加载流程
     *
     * @param value 是否停止
     */
    public static void setStopped(boolean value) {
        isStopped = value;
    }

    /**
     * 获取当前插件所有被唤醒的类
     */
    public static Map<String, Object> getAwakenedClasses() {
        return awakenedClasses;
    }

    /**
     * 获取类
     * 由于 Paper 1.20.6+ 会在运行时修改字节码以接管插件的 Class.forName 调用，
     * 因此 TabooLib 的外部模块均需要通过此方法获取类，以借助 Paper 的重定向机制。
     */
    public static Class<?> getClass(String name) throws ClassNotFoundException {
        return classFinder.getClass(name);
    }

    /**
     * 获取类
     * 逆天 Paper，这俩玩意儿反而不接管，想不明白。
     */
    public static Class<?> getClass(String name, boolean initialize) throws ClassNotFoundException {
        return classFinder.getClass(name, initialize);
    }

    public static Class<?> getClass(String name, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
        return classFinder.getClass(name, initialize, classLoader);
    }

    public static void setClassFinder(ClassFinder classFinder) {
        TabooLib.classFinder = classFinder;
    }

    public static ClassFinder getClassFinder() {
        return classFinder;
    }

    public static abstract class ClassFinder {

        public abstract Class<?> getClass(String name) throws ClassNotFoundException;

        public abstract Class<?> getClass(String name, boolean initialize) throws ClassNotFoundException;

        public abstract Class<?> getClass(String name, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException;
    }
}
