package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import java.util.function.Supplier

class AwakeFunction(private val lifeCycle: LifeCycle) : ClassVisitor(0) {

    override fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(Awake::class.java) && method.getAnnotation(Awake::class.java).enum<LifeCycle>("value", LifeCycle.ENABLE) == lifeCycle) {
            if (instance != null) {
                method.invoke(instance.get())
            } else {
                method.invokeStatic()
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return lifeCycle
    }

    override fun toString(): String {
        return "AwakeFunction(lifeCycle=$lifeCycle)"
    }
}