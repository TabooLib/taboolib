package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.function.submit

@Awake
@Inject
class ClassVisitorSchedule : ClassVisitor(1) {

    override fun visit(method: ClassMethod, owner: ReflexClass) {
        val annotation = method.getAnnotationIfPresent(Schedule::class.java) ?: return
        val instance = findInstance(owner)
        submit(async = annotation.property("async", false), delay = annotation.property("delay", 0), period = annotation.property("period", 0)) {
            if (instance != null) {
                method.invoke(instance)
            } else {
                method.invokeStatic()
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ACTIVE
    }
}