@file:Isolated

package taboolib.common.inject

import taboolib.common.Isolated

inline fun <reified T> inject() {
    return InjectorFactory.INSTANCE.inject(T::class.java)
}