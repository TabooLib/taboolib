package taboolib.module.incision.pred;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 谓词运行时通用算子（被 PredCompiler 生成的字节码 INVOKESTATIC 进来）。
 *
 * <p>放在 java 包以避免 Kotlin metadata / 重定位影响；所有方法的签名都使用 Object/boolean
 * 等基础类型，便于 ASM 在不知道实际运行期类型的情况下生成调用。
 *
 * <p>语义见 PredAst 注释；此类不应抛 RuntimeException 给字节码，错误一律转为 false 或
 * 通过 throw Trauma.Predicate.* 由上层 dispatcher 包装。
 */
public final class PredOps {

    private PredOps() {}

    // ---- 比较 ----

    public static boolean eq(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        return a.equals(b);
    }

    public static boolean neq(Object a, Object b) { return !eq(a, b); }

    public static boolean lt(Object a, Object b) { return a != null && b != null && cmp(a, b) < 0; }
    public static boolean gt(Object a, Object b) { return a != null && b != null && cmp(a, b) > 0; }
    public static boolean le(Object a, Object b) { return a != null && b != null && cmp(a, b) <= 0; }
    public static boolean ge(Object a, Object b) { return a != null && b != null && cmp(a, b) >= 0; }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int cmp(Object a, Object b) {
        if (a == null || b == null) return 0; // 与 null 比较一律视为 0，由 eq/neq 单独处理
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            return Double.compare(da, db);
        }
        if (a instanceof Comparable && a.getClass().isInstance(b)) {
            return ((Comparable) a).compareTo(b);
        }
        return 0;
    }

    public static boolean matches(Object a, Object regex) {
        if (a == null || regex == null) return false;
        Pattern p = patternCache.computeIfAbsent(regex.toString(), Pattern::compile);
        return p.matcher(a.toString()).matches();
    }

    private static final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<String, Pattern>();

    /** `expr in collection` */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean contains(Object element, Object collection) {
        if (collection == null) return false;
        if (collection instanceof java.util.Collection) {
            return ((java.util.Collection) collection).contains(element);
        }
        if (collection instanceof Map) {
            return ((Map) collection).containsKey(element);
        }
        if (collection instanceof Object[]) {
            for (Object o : (Object[]) collection) {
                if (eq(o, element)) return true;
            }
            return false;
        }
        if (collection instanceof CharSequence && element != null) {
            return collection.toString().contains(element.toString());
        }
        return false;
    }

    // ---- 类型算子 ----

    public static boolean isInstanceOf(Object x, Class<?> t) {
        return x != null && t != null && t.isInstance(x);
    }

    /** ic：INSTANCEOF + 不等 class（即子类型且不是同一个类） */
    public static boolean isInstanceChild(Object x, Class<?> t) {
        if (x == null || t == null) return false;
        return t.isInstance(x) && x.getClass() != t;
    }

    /** ip：T.isAssignableFrom(x.getClass())（与 isInstance 类似，但 x 为 Class 时检查 Class 继承） */
    public static boolean isAssignable(Object x, Class<?> t) {
        if (x == null || t == null) return false;
        Class<?> xc = (x instanceof Class<?>) ? (Class<?>) x : x.getClass();
        return t.isAssignableFrom(xc);
    }

    /** it：x.getClass() == T */
    public static boolean isExactType(Object x, Class<?> t) {
        return x != null && t != null && x.getClass() == t;
    }

    /** as：失败返回 null，谓词层将 null 视为 false。 */
    public static Object asCast(Object x, Class<?> t) {
        if (x == null || t == null) return null;
        return t.isInstance(x) ? x : null;
    }

    // ---- 成员访问 ----

    /**
     * 反射读取属性 / Kotlin 属性 / get/is 前缀方法。
     * 对应 PropertyAccess。失败一律返回 null（由谓词语义层决定 false / 短路）。
     */
    public static Object getProperty(Object receiver, String name) {
        if (receiver == null) return null;
        Class<?> c = receiver.getClass();
        // 字段
        try {
            Field f = c.getField(name);
            return f.get(receiver);
        } catch (NoSuchFieldException ignore) {
        } catch (Throwable ignore) { return null; }
        // getXxx
        String capitalized = name.length() == 0 ? name :
                Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method m = c.getMethod("get" + capitalized);
            return m.invoke(receiver);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable ignore) { return null; }
        try {
            Method m = c.getMethod("is" + capitalized);
            return m.invoke(receiver);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable ignore) { return null; }
        // 直接同名（Kotlin 字段反射不到也走这里）
        try {
            Method m = c.getMethod(name);
            return m.invoke(receiver);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable ignore) { return null; }
        return null;
    }

    /**
     * 反射调用方法。args 为可变参数 Object 数组。
     * 简化匹配：按形参个数定位，若多个候选则按类型兼容性挑第一个；否则 null。
     */
    public static Object callMethod(Object receiver, String name, Object[] args) {
        if (receiver == null) return null;
        Class<?> c = receiver.getClass();
        Method best = null;
        Method[] all = c.getMethods();
        for (Method m : all) {
            if (!m.getName().equals(name)) continue;
            if (m.getParameterCount() != (args == null ? 0 : args.length)) continue;
            if (matchesParams(m, args)) { best = m; break; }
            if (best == null) best = m; // arity 匹配的兜底
        }
        if (best == null) return null;
        try { return best.invoke(receiver, args); }
        catch (Throwable t) { return null; }
    }

    private static boolean matchesParams(Method m, Object[] args) {
        Class<?>[] ps = m.getParameterTypes();
        if (args == null) return ps.length == 0;
        for (int i = 0; i < ps.length; i++) {
            Object a = args[i];
            if (a == null) {
                if (ps[i].isPrimitive()) return false;
                continue;
            }
            Class<?> p = box(ps[i]);
            if (!p.isInstance(a)) return false;
        }
        return true;
    }

    private static Class<?> box(Class<?> p) {
        if (!p.isPrimitive()) return p;
        if (p == int.class) return Integer.class;
        if (p == long.class) return Long.class;
        if (p == double.class) return Double.class;
        if (p == float.class) return Float.class;
        if (p == boolean.class) return Boolean.class;
        if (p == byte.class) return Byte.class;
        if (p == char.class) return Character.class;
        if (p == short.class) return Short.class;
        return p;
    }

    /** 下标访问：List / Map / Object[] / CharSequence。 */
    public static Object index(Object receiver, Object key) {
        if (receiver == null) return null;
        if (receiver instanceof List) {
            int i = ((Number) key).intValue();
            List<?> l = (List<?>) receiver;
            return (i >= 0 && i < l.size()) ? l.get(i) : null;
        }
        if (receiver instanceof Map) {
            return ((Map<?, ?>) receiver).get(key);
        }
        if (receiver instanceof Object[]) {
            int i = ((Number) key).intValue();
            Object[] arr = (Object[]) receiver;
            return (i >= 0 && i < arr.length) ? arr[i] : null;
        }
        if (receiver instanceof CharSequence) {
            int i = ((Number) key).intValue();
            CharSequence s = (CharSequence) receiver;
            return (i >= 0 && i < s.length()) ? Character.valueOf(s.charAt(i)) : null;
        }
        return null;
    }

    // ---- 真值化 ----

    /** Object → boolean：null/false → false，其余 true（数字 0 仍按真）。 */
    public static boolean truthy(Object x) {
        if (x == null) return false;
        if (x instanceof Boolean) return ((Boolean) x).booleanValue();
        return true;
    }

    /** 把 boolean 装回 Object，方便 cmp/and/or 中转。 */
    public static Object box(boolean b) { return Boolean.valueOf(b); }

    // ---- 类型查找（缓存） ----

    private static final ConcurrentHashMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();

    public static Class<?> resolveType(String name, ClassLoader cl) {
        Class<?> c = classCache.get(name);
        if (c != null) return c;
        ClassLoader[] loaders = new ClassLoader[] {
            cl,
            Thread.currentThread().getContextClassLoader(),
            PredOps.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
        };
        for (ClassLoader loader : loaders) {
            if (loader == null) continue;
            try {
                c = Class.forName(name, false, loader);
                classCache.put(name, c);
                return c;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
