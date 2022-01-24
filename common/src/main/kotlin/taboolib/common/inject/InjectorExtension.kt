@file:Isolated

package taboolib.common.inject

import taboolib.common.Isolated

inline fun <reified T> inject() {
    return InjectorFactory.injectIgnoreLifeCycle(T::class.java)
}