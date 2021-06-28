package taboolib.common.inject

import taboolib.common.LifeCycle

interface InjectorOrder {

    val lifeCycle: LifeCycle

    val priority: Byte
}