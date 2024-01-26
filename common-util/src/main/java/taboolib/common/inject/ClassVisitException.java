package taboolib.common.inject;

import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import taboolib.common.LifeCycle;

/**
 * TabooLib
 * taboolib.common.inject.ClassVisitException
 *
 * @author 坏黑
 * @since 2022/8/5 20:27
 */
public class ClassVisitException extends RuntimeException {

    public ClassVisitException(Class<?> clazz, Throwable cause) {
        super(clazz.toString(), cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, Throwable cause) {
        super(clazz + ": " + group + " (" + lifeCycle + ")", cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ClassField field, Throwable cause) {
        super(clazz + "#" + field.getName() + ": " + group + " (" + lifeCycle + ")", cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ClassMethod method, Throwable cause) {
        super(clazz + "#" + method.getName() + ": " + group + " (" + lifeCycle + ")", cause);
    }
}
