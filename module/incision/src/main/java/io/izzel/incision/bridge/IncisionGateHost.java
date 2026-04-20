package io.izzel.incision.bridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 系统级 IncisionGate 宿主 — 由 GateBootstrapper 通过
 * <code>Instrumentation.appendToSystemClassLoaderSearch</code> 推入系统 ClassLoader。
 *
 * 设计：
 * - 仅持有 <code>target → List&lt;AdviceProxy&gt;</code> 链表
 * - <code>AdviceProxy</code> 是反射代理对象，本类通过反射调用其 <code>invoke</code>，
 *   不需要类型一致（避免跨 ClassLoader 类型问题）
 * - 公开 <code>INSTANCE</code> 静态字段供 IncisionBridge 反射拿取
 *
 * 不依赖 Kotlin、不依赖 taboolib 包名 — 因此在系统 ClassLoader 上加载安全。
 */
public final class IncisionGateHost {

    public static final IncisionGateHost INSTANCE = new IncisionGateHost();

    /** target signature → advice proxies */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> chains = new ConcurrentHashMap<String, CopyOnWriteArrayList<Object>>();

    /** AdviceProxy class → invoke Method 缓存 */
    private final ConcurrentHashMap<Class<?>, Method> invokeCache = new ConcurrentHashMap<Class<?>, Method>();

    private IncisionGateHost() {}

    /** advice 注册：proxy 必须有 invoke(String, Object, Object[]) 方法 */
    public void register(String targetSignature, Object adviceProxy) {
        chains.computeIfAbsent(targetSignature, new java.util.function.Function<String, CopyOnWriteArrayList<Object>>() {
            @Override public CopyOnWriteArrayList<Object> apply(String s) { return new CopyOnWriteArrayList<Object>(); }
        }).add(adviceProxy);
        sortByPriority(targetSignature);
    }

    public boolean unregister(String targetSignature, Object adviceProxy) {
        CopyOnWriteArrayList<Object> list = chains.get(targetSignature);
        return list != null && list.remove(adviceProxy);
    }

    public Object dispatch(String targetSignature, Object self, Object[] args) {
        CopyOnWriteArrayList<Object> list = chains.get(targetSignature);
        if (list == null || list.isEmpty()) return null;
        Object result = null;
        for (Object proxy : list) {
            try {
                Method m = invokeCache.get(proxy.getClass());
                if (m == null) {
                    m = proxy.getClass().getMethod("invoke", String.class, Object.class, Object[].class);
                    invokeCache.put(proxy.getClass(), m);
                }
                Object r = m.invoke(proxy, targetSignature, self, args);
                if (r != null) result = r;
            } catch (Throwable t) {
                System.err.println("[Incision][Host] advice invoke failed: " + t);
            }
        }
        return result;
    }

    public List<String> listTargets() {
        return new ArrayList<String>(chains.keySet());
    }

    public int healByClassLoader(ClassLoader cl) {
        int n = 0;
        for (CopyOnWriteArrayList<Object> list : chains.values()) {
            List<Object> rm = new ArrayList<Object>();
            for (Object p : list) {
                ClassLoader pcl = p.getClass().getClassLoader();
                if (pcl == cl) rm.add(p);
            }
            n += rm.size();
            list.removeAll(rm);
        }
        return n;
    }

    private void sortByPriority(String targetSignature) {
        CopyOnWriteArrayList<Object> list = chains.get(targetSignature);
        if (list == null || list.size() < 2) return;
        List<Object> snapshot = new ArrayList<Object>(list);
        snapshot.sort(new java.util.Comparator<Object>() {
            @Override public int compare(Object a, Object b) {
                return Integer.compare(priorityOf(b), priorityOf(a));
            }
        });
        list.clear();
        list.addAll(snapshot);
    }

    private int priorityOf(Object proxy) {
        try {
            Method m = proxy.getClass().getMethod("priority");
            return ((Number) m.invoke(proxy)).intValue();
        } catch (Throwable t) {
            return 0;
        }
    }

    public int apiVersion() { return 1; }
}
