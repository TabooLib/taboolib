package taboolib.internal

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector

/**
 * TabooLib
 * RegisteredInjector
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
class RegisteredInjector(val injector: Injector) {

    val type: List<Class<*>>
    val target: Bind.Target
    val annotation: List<Class<out Annotation>>

    init {
        if (injector.javaClass.isAnnotationPresent(Bind::class.java)) {
            val bind = injector.javaClass.getAnnotation(Bind::class.java)
            type = bind.type.map { it.java }
            target = bind.target
            annotation = bind.value.map { it.java }
        } else {
            type = emptyList()
            target = Bind.Target.ALL
            annotation = emptyList()
        }
    }

    fun checkType(type: Class<*>): Boolean {
        return this.type.isEmpty() || this.type.any { it.isAssignableFrom(type) }
    }

    fun checkTarget(target: Bind.Target): Boolean {
        return this.target == Bind.Target.ALL || this.target == target
    }

    fun check(target: Class<*>): Boolean {
        return (annotation.isEmpty() || annotation.any { target.isAnnotationPresent(it) }) && checkType(target)
    }

    fun check(target: ClassField): Boolean {
        return (annotation.isEmpty() || annotation.any { target.isAnnotationPresent(it) }) && checkType(target.fieldType)
    }

    fun check(target: ClassMethod): Boolean {
        return (annotation.isEmpty() || annotation.any { target.isAnnotationPresent(it) })
    }
}