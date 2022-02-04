@file:Isolated

package taboolib.platform.util

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEditBookEvent
import taboolib.common.Isolated
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import java.util.concurrent.ConcurrentHashMap

/**
 * 向玩家发送一本书
 * 并捕获该书本的编辑动作
 *
 * @param display    展示名称
 * @param disposable 编辑后销毁
 * @param content     原始内容
 * @param catcher    编辑动作
 */
fun Player.inputBook(display: String, disposable: Boolean = true, content: List<String> = emptyList(), catcher: (List<String>) -> Unit) {
    // 移除正在编辑的书本
    inventory.takeItem(99) { i -> i.hasLore(BookListener.regex[0]) }
    // 发送书本
    giveItem(
        buildBook {
            write(content.joinToString("\n"))
            setMaterial(XMaterial.WRITABLE_BOOK)
            name = "§f$display"
            lore += BookListener.regex[0]
            lore += if (disposable) {
                BookListener.regex[1]
            } else {
                BookListener.regex[2]
            }
        }
    )
    BookListener.inputs[name] = catcher
}

@Isolated
@PlatformSide([Platform.BUKKIT])
internal object BookListener {

    internal val regex = arrayOf("§7Right-Click to open and write.", "§cDisposable", "§aReusable")

    internal val inputs = ConcurrentHashMap<String, (List<String>) -> Unit>()

    @SubscribeEvent
    fun onPlayerEditBookEvent(event: PlayerEditBookEvent) {
        val lore = event.newBookMeta.lore
        if (lore != null && lore.getOrNull(0) == regex[0]) {
            val consumer = inputs[event.player.name] ?: return
            val pages = event.newBookMeta.pages.flatMap { TextComponent(it).toPlainText().replace("§0", "").split("\n") }
            consumer(pages)
            if (lore.getOrNull(1) == regex[1]) {
                inputs.remove(event.player.name)
                event.player.inventory.takeItem(99) { i -> i.hasLore(regex[0]) }
            }
        }
    }
}