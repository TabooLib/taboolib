package taboolib.module.ui.type

import taboolib.module.ui.type.impl.ChestImpl

/**
 * 向下兼容
 */
@Deprecated("Use Chest instead.", ReplaceWith("Chest"))
open class Basic(override var title: String) : ChestImpl(title)