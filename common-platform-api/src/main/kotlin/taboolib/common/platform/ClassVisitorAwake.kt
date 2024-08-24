package taboolib.common.platform

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.inject.ClassVisitor

class ClassVisitorAwake(private val lifeCycle: LifeCycle) : ClassVisitor(0) {

    override fun visit(method: ClassMethod, owner: ReflexClass) {
        if (method.getAnnotationIfPresent(Awake::class.java)?.enumName("value") == lifeCycle.name) {
            val instance = findInstance(owner)
            if (instance != null) {
                method.invoke(instance)
            } else {
                method.invokeStatic()
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return lifeCycle
    }
}