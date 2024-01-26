package taboolib.platform.lang

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.common.util.replaceWithOrder
import taboolib.common5.cdouble
import taboolib.common5.clong
import taboolib.module.lang.Language
import taboolib.module.lang.Type

/**
 * TabooLib
 * taboolib.module.lang.TypeBossBar
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeBossBar : Type {

    var text: String? = null
    var color = BarColor.WHITE
    var style = BarStyle.SOLID
    var step = 0.01
    var period = 2L
    var method = "INCREASE"

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.toString()
        color = BarColor.values().find { it.name == source["color"].toString().uppercase() } ?: BarColor.WHITE
        style = BarStyle.values().find { it.name == source["style"].toString().uppercase() } ?: BarStyle.SOLID
        step = source["step"]?.cdouble ?: 0.01
        period = source["period"]?.clong ?: 2L
        method = source["method"]?.toString()?.uppercase() ?: "INCREASE"
        // 合法性检查
        if (text == null) {
            warning("Missing BossBar text.")
        }
        if (method != "INCREASE" && method != "DECREASE") {
            warning("Unknown method $method, use INCREASE or DECREASE.")
        }
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        if (text == null) {
            return
        }
        if (sender is ProxyPlayer) {
            val bossBar = Bukkit.createBossBar(text!!.translate(sender, *args).replaceWithOrder(*args), color, style)
            bossBar.progress = if (method == "INCREASE") 0.0 else 1.0
            bossBar.addPlayer(sender.cast())
            submit(period = period) {
                val progress = bossBar.progress + if (method == "INCREASE") step else -step
                if (progress in 0.0..1.0) {
                    bossBar.progress = progress
                } else {
                    bossBar.removeAll()
                    cancel()
                }
            }
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "TypeBossBar(text=$text, color=$color, style=$style, step=$step, period=$period, method='$method')"
    }

    @Inject
    @PlatformSide(Platform.BUKKIT)
    internal companion object {

        @Awake(LifeCycle.CONST)
        fun init() {
            Language.languageType["boss"] = TypeBossBar::class.java
            Language.languageType["bossbar"] = TypeBossBar::class.java
        }
    }
}