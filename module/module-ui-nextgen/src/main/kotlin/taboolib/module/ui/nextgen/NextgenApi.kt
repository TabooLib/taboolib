package taboolib.module.ui.nextgen

import org.bukkit.entity.Player
import taboolib.module.ui.nextgen.api.NextgenUI
import taboolib.module.ui.nextgen.internal.SimpleNextgenUI

fun newUI(viewer: Player, show: Boolean = true, block: NextgenUI.() -> Unit) {
    val ui = NextgenUI.create(viewer).apply(block).also { it.inited() }
    if (show) ui.render()
}

fun newUI(block: NextgenUI.() -> Unit): NextgenUI {
    return NextgenUI.create().apply(block)
}
