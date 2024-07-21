package taboolib.module.ui.type

import taboolib.module.ui.type.impl.StorableChestImpl

/**
 * 向下兼容
 */
@Deprecated("Use StorableChest instead.", ReplaceWith("StorableChest"))
open class Stored(override var title: String) : StorableChestImpl(title)