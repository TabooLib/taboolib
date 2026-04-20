package taboolib.module.incision.weaver

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class BridgeClassLoader private constructor() : ClassLoader(null) {

    companion object {

        val INSTANCE = BridgeClassLoader()

        const val BODIES_SUFFIX = "$\$IncisionBodies"

        fun bodiesClassName(ownerInternalName: String): String {
            return ownerInternalName.replace('/', '.') + BODIES_SUFFIX
        }
    }

    private val definedClasses = ConcurrentHashMap<String, Class<*>>()
    private val delegates = CopyOnWriteArrayList<WeakReference<ClassLoader>>()

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        definedClasses[name]?.let { return it }
        for (ref in delegates) {
            val cl = ref.get() ?: continue
            try {
                return cl.loadClass(name)
            } catch (_: ClassNotFoundException) {
            }
        }
        return super.loadClass(name, resolve)
    }

    fun defineBodies(binaryName: String, bytes: ByteArray): Class<*> {
        definedClasses[binaryName]?.let { return it }
        val cls = defineClass(binaryName, bytes, 0, bytes.size)
        definedClasses[binaryName] = cls
        return cls
    }

    fun registerDelegate(cl: ClassLoader) {
        for (ref in delegates) {
            if (ref.get() === cl) return
        }
        delegates.add(WeakReference(cl))
    }

    fun unregisterDelegate(cl: ClassLoader) {
        delegates.removeIf { it.get() === cl || it.get() == null }
    }

    fun hasBodies(binaryName: String): Boolean = definedClasses.containsKey(binaryName)

    fun cleanup() {
        delegates.removeIf { it.get() == null }
    }
}
