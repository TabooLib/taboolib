package taboolib.module.ui.nextgen.internal

import taboolib.module.ui.nextgen.api.NuiElement

data class LocatedNuiElement(
    val rows: Int,
    val columns: Int,
    val element: NuiElement
)
