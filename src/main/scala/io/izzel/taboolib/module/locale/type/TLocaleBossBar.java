package io.izzel.taboolib.module.locale.type;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.kotlin.kether.KetherFunction;
import io.izzel.taboolib.kotlin.kether.ScriptContext;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleSerialize;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author sky
 * @since 2018-05-27 18:52
 */
@ThreadSafe
@SerializableAs("BAR")
public class TLocaleBossBar extends TLocaleSerialize {

    /*
        BossBar:
        - ==: BAR
          text: 'BossBar 血条公告'
          color: BLUE
          style: NOTCHED_20
          progress: 1.0
          timeout: 20
          timeout-interval: 2
     */

    private final String text;
    private final BarColor color;
    private final BarStyle style;
    private final float progress;
    private final int timeout;

    public TLocaleBossBar(String text, BarColor color, BarStyle style, float progress, int timeout, boolean papi, boolean kether) {
        super(papi, kether);
        this.text = text;
        this.color = color;
        this.style = style;
        this.progress = progress;
        this.timeout = timeout;
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            String title = TLocale.Translate.setColored(Strings.replaceWithOrder(text, args));
            if (papi) {
                title = TLocale.Translate.setPlaceholders(sender, title);
            }
            if (kether) {
                title = KetherFunction.INSTANCE.parse(title, false, true, Collections.emptyList(), new Consumer<ScriptContext>() {
                    @Override
                    public void accept(ScriptContext context) {
                        context.setSender(sender);
                    }
                });
            }
            BossBar bossBar = Bukkit.createBossBar(title, color, style);
            bossBar.setProgress(progress);
            bossBar.addPlayer((Player) sender);
            if (timeout > 0) {
                Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), bossBar::removeAll, timeout);
            }
        } else {
            sender.sendMessage(text);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    public static TLocaleBossBar valueOf(Map<String, Object> map) {
        return new TLocaleBossBar(
                map.getOrDefault("text", "§4* Invalid Text*").toString(),
                getColor(String.valueOf(map.get("color"))),
                getStyle(String.valueOf(map.get("style"))),
                NumberConversions.toFloat(map.getOrDefault("progress", 1)),
                NumberConversions.toInt(map.getOrDefault("timeout", 20)),
                isPlaceholderEnabled(map),
                isKetherEnabled(map));
    }

    private static BarColor getColor(String color) {
        try {
            return BarColor.valueOf(color);
        } catch (Exception e) {
            TLocale.Logger.error("LOCALE.BAR-STYLE-IDENTIFICATION-FAILED", e.toString());
            return BarColor.WHITE;
        }
    }

    private static BarStyle getStyle(String style) {
        try {
            return BarStyle.valueOf(style);
        } catch (Exception e) {
            TLocale.Logger.error("LOCALE.BAR-COLOR-IDENTIFICATION-FAILED", e.toString());
            return BarStyle.SEGMENTED_20;
        }
    }
}
