package io.izzel.taboolib.module.locale.type;

import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleSerialize;
import io.izzel.taboolib.util.Strings;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-05-27 18:52
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
    private final BossBarAPI.Color color;
    private final BossBarAPI.Style style;
    private final float progress;
    private final int timeout;
    private final int timeoutInterval;
    private final boolean papi;

    public TLocaleBossBar(String text, BossBarAPI.Color color, BossBarAPI.Style style, float progress, int timeout, int timeoutInterval, boolean papi) {
        this.text = text;
        this.color = color;
        this.style = style;
        this.progress = progress;
        this.timeout = timeout;
        this.timeoutInterval = timeoutInterval;
        this.papi = papi;
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (Bukkit.getPluginManager().getPlugin("BossBarAPI") == null) {
            TLocale.Logger.error("LOCALE.BAR-PLUGIN-NOT-FOUND");
            return;
        }
        if (sender instanceof Player) {
            TextComponent textComponent = new TextComponent(papi ? TLocale.Translate.setPlaceholders(sender, Strings.replaceWithOrder(text, args)) : TLocale.Translate.setColored(Strings.replaceWithOrder(text, args)));
            BossBar bossBar = BossBarAPI.addBar((Player) sender, textComponent, color, style, progress, timeout, timeoutInterval);
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
                NumberConversions.toInt(map.getOrDefault("timeout-interval", 2)),
                isPlaceholderEnabled(map));
    }

    private static BossBarAPI.Color getColor(String color) {
        try {
            return BossBarAPI.Color.valueOf(color);
        } catch (Exception e) {
            TLocale.Logger.error("LOCALE.BAR-STYLE-IDENTIFICATION-FAILED", e.toString());
            return BossBarAPI.Color.WHITE;
        }
    }

    private static BossBarAPI.Style getStyle(String style) {
        try {
            return BossBarAPI.Style.valueOf(style);
        } catch (Exception e) {
            TLocale.Logger.error("LOCALE.BAR-COLOR-IDENTIFICATION-FAILED", e.toString());
            return BossBarAPI.Style.NOTCHED_20;
        }
    }
}
