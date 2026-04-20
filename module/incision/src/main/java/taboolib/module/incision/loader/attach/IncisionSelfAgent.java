package taboolib.module.incision.loader.attach;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public final class IncisionSelfAgent {

    private IncisionSelfAgent() {}

    public static void premain(String args, Instrumentation inst) { inject(args, inst); }
    public static void agentmain(String args, Instrumentation inst) { inject(args, inst); }

    private static void inject(String targetClassName, Instrumentation inst) {
        // targetClassName is passed from ManualSelfAttach as the actual (possibly relocated) class name
        if (targetClassName != null && !targetClassName.isEmpty()) {
            for (Class<?> cls : inst.getAllLoadedClasses()) {
                if (cls.getName().equals(targetClassName)) {
                    if (trySetField(cls, inst)) return;
                }
            }
        }
        // fallback: search by simple name suffix
        for (Class<?> cls : inst.getAllLoadedClasses()) {
            if (cls.getName().endsWith(".loader.attach.ManualSelfAttach")) {
                if (trySetField(cls, inst)) return;
            }
        }
    }

    private static boolean trySetField(Class<?> cls, Instrumentation inst) {
        try {
            Field f = cls.getDeclaredField("captured");
            f.setAccessible(true);
            f.set(null, inst);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
