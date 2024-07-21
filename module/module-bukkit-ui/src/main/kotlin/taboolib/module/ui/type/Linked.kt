package taboolib.module.ui.type

import taboolib.module.ui.type.impl.PageableChestImpl

/**
 * 向下兼容
 */
@Deprecated("Use PageableChest instead.", ReplaceWith("PageableChest"))
open class Linked<T>(override var title: String) : PageableChestImpl<T>(title)