package io.izzel.incision.bridge;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Incision 字节码桥 — 所有被 incision 织入的 INVOKESTATIC 调用都指向这里。
 *
 * 设计背景：
 * TabooLib Gradle 插件 (<code>io.izzel.taboolib</code>) 的 RelocateRemapper 会在
 * 打包阶段把 <code>taboolib.*</code> 前缀重定向为 <code>&lt;user.group&gt;.taboolib.*</code>。
 * 如果桥类放在 <code>taboolib.module.incision.*</code>，每个插件最终 jar 中的桥
 * 会变成不同类，无法跨插件共享。
 *
 * 因此桥必须位于 <b>taboolib.* 之外</b> 的包（本类位于 <code>io.izzel.incision.bridge</code>），
 * 并且用 Java 编写以避免 Kotlin 运行时依赖（Kotlin 同样会被 <code>kotlin.*</code> 重定向）。
 *
 * 解析顺序：
 * <ol>
 *   <li>优先查系统 ClassLoader 上的 <code>io.izzel.incision.bridge.IncisionGateHost</code>
 *       （由 GateBootstrapper 通过 appendToSystemClassLoaderSearch 推入），
 *       拿到之后所有插件共享同一宿主；</li>
 *   <li>若系统 ClassLoader 上没有宿主，退化为当前调用方 ClassLoader 的本地 TheatreDispatcher
 *       （单插件场景正常工作）。</li>
 * </ol>
 *
 * 本类通过反射跨 ClassLoader 调用 dispatch，避免类型一致性问题。
 */
public final class IncisionBridge {

    private IncisionBridge() {}

    private static final Object BYPASS_MISS = new Object();

    /** 系统 ClassLoader 上的宿主类（若存在） */
    private static volatile Object systemHost = findSystemHost();

    /** 宿主的 dispatch 方法反射缓存 */
    private static volatile Method systemDispatch = resolveDispatch(systemHost);

    /** ClassLoader → 本地 TheatreDispatcher.dispatch Method 缓存（单插件 fallback 路径） */
    private static final ConcurrentHashMap<ClassLoader, Method> localCache = new ConcurrentHashMap<ClassLoader, Method>();

    /** 全局回退 — 任何 ClassLoader 都能命中的本地 dispatcher */
    private static volatile Method fallbackLocalDispatch = null;

    /**
     * 供 weaver 注入的 INVOKESTATIC 目标。
     *
     * <pre>
     * INVOKESTATIC io/izzel/incision/bridge/IncisionBridge.dispatch
     *   (Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
     * </pre>
     *
     * @param targetSignature 目标方法签名（编译期常量串）
     * @param self            this 引用（静态方法时为 null）
     * @param args            原方法实参
     * @return advice 链的最终返回值；若为 null 调用方应继续执行原方法
     */
    public static Object dispatch(String targetSignature, Object self, Object[] args) {
        // 优先走系统宿主
        Method m = systemDispatch;
        Object host = systemHost;
        if (m != null && host != null) {
            try {
                return m.invoke(host, targetSignature, self, args);
            } catch (Throwable t) {
                // 宿主异常不应阻断原方法
                System.err.println("[Incision][Bridge] system host dispatch failed: " + t);
            }
        }

        // 本地 fallback — 从调用方 ClassLoader 查 TheatreDispatcher
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = IncisionBridge.class.getClassLoader();
        Method local = resolveLocalDispatch(cl);
        if (local != null) {
            try {
                if (local.getParameterCount() == 4) {
                    return local.invoke(null, targetSignature, self, args, null);
                } else {
                    return local.invoke(null, targetSignature, self, args);
                }
            } catch (Throwable t) {
                System.err.println("[Incision][Bridge] local dispatch failed: " + t);
            }
        }
        return null;
    }

    public static Object dispatchBypass(String targetSignature, Object self, Object[] args) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = IncisionBridge.class.getClassLoader();
        Method local = resolveLocalSibling(cl, "dispatchBypass");
        if (local != null) {
            try {
                return local.invoke(null, targetSignature, self, args);
            } catch (Throwable t) {
                System.err.println("[Incision][Bridge] local bypass dispatch failed: " + t);
            }
        }
        return BYPASS_MISS;
    }

    public static Object bypassMiss() {
        return BYPASS_MISS;
    }

    public static boolean isBypassMiss(Object value) {
        return value == BYPASS_MISS;
    }

    /** 宿主绑定入口 — GateBootstrapper 创建 host 后调用此方法完成注册 */
    public static synchronized void bindSystemHost(Object host) {
        systemHost = host;
        systemDispatch = resolveDispatch(host);
    }

    public static synchronized void unbindSystemHost() {
        systemHost = null;
        systemDispatch = null;
    }

    public static boolean hasSystemHost() {
        return systemHost != null && systemDispatch != null;
    }

    // -----------------------------------------------------------------

    private static Object findSystemHost() {
        try {
            ClassLoader sys = ClassLoader.getSystemClassLoader();
            Class<?> hostCls = Class.forName("io.izzel.incision.bridge.IncisionGateHost", true, sys);
            // 约定：IncisionGateHost.INSTANCE 是公开静态字段
            return hostCls.getField("INSTANCE").get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method resolveDispatch(Object host) {
        if (host == null) return null;
        try {
            return host.getClass().getMethod(
                "dispatch", String.class, Object.class, Object[].class
            );
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method resolveLocalDispatch(ClassLoader cl) {
        Method cached = localCache.get(cl);
        if (cached != null) return cached;
        // 全局回退 — ClassLoader 不匹配时使用 registerLocalDispatcher 注册的方法
        return fallbackLocalDispatch;
    }

    /** 由 IncisionBootstrap 在 CONST 阶段调用，显式注册经过重定向后的 dispatcher 类 */
    public static void registerLocalDispatcher(Class<?> dispatcherClass) {
        if (dispatcherClass == null) return;
        Method best = pickDispatchMethod(dispatcherClass);
        if (best != null) {
            localCache.put(dispatcherClass.getClassLoader(), best);
            fallbackLocalDispatch = best;
        }
    }

    /** 借鉴 MeteorInjector 的 getMethod(clazz, returnType, index) 风格 — 形状优先，名字次之 */
    private static Method pickDispatchMethod(Class<?> cls) {
        Method named3 = null, named4 = null, anyShape3 = null, anyShape4 = null;
        for (Method m : cls.getMethods()) {
            Class<?>[] pt = m.getParameterTypes();
            boolean shape3 = pt.length == 3 && pt[0] == String.class && pt[1] == Object.class && pt[2] == Object[].class;
            boolean shape4 = pt.length == 4 && pt[0] == String.class && pt[1] == Object.class && pt[2] == Object[].class;
            if (m.getName().equals("dispatch")) {
                if (shape3) named3 = m;
                else if (shape4) named4 = m;
            }
            if (anyShape3 == null && shape3) anyShape3 = m;
            if (anyShape4 == null && shape4) anyShape4 = m;
        }
        if (named4 != null) return named4;
        if (named3 != null) return named3;
        return anyShape4 != null ? anyShape4 : anyShape3;
    }

    private static Method resolveLocalSibling(ClassLoader cl, String methodName) {
        Method local = resolveLocalDispatch(cl);
        if (local == null) return null;
        try {
            return local.getDeclaringClass().getMethod(methodName, String.class, Object.class, Object[].class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** 插件 DISABLE 时调用 — 移除该 ClassLoader 关联的本地 dispatcher 缓存 */
    public static void unregisterLocalDispatcher(ClassLoader cl) {
        if (cl != null) localCache.remove(cl);
    }
}
