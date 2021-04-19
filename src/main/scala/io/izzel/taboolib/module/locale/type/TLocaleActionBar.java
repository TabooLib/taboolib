package io.izzel.taboolib.module.locale.type;

import com.google.common.collect.Maps;
import io.izzel.taboolib.kotlin.kether.KetherFunction;
import io.izzel.taboolib.kotlin.kether.ScriptContext;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleSerialize;
import io.izzel.taboolib.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@Immutable
@SerializableAs("ACTION")
public class TLocaleActionBar extends TLocaleSerialize {

    private final String text;

    public TLocaleActionBar(String text, boolean papi, boolean kether) {
        super(papi, kether);
        this.text = text;
    }

    public static TLocaleActionBar valueOf(Map<String, Object> map) {
        String text = ChatColor.translateAlternateColorCodes('&', String.valueOf(map.getOrDefault("text", "Empty Action bar message.")));
        return new TLocaleActionBar(text, isPlaceholderEnabled(map), isKetherEnabled(map));
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            TLocale.Display.sendActionBar(((Player) sender), replace(sender, text, args));
        }
    }

    private String replace(CommandSender sender, String text, String[] args) {
        String s = TLocale.Translate.setColored(Strings.replaceWithOrder(text, args));
        if (papi) {
            s = PlaceholderHook.replace(sender, s);
        }
        if (kether) {
            s = KetherFunction.INSTANCE.parse(s, false, true, Collections.emptyList(), new Consumer<ScriptContext>() {
                @Override
                public void accept(ScriptContext context) {
                    context.setSender(sender);
                }
            });
        }
        return s;
    }

    @Override
    public String asString(String... args) {
        return "ActionBar: [" + text + "]";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("text", text);
        if (papi) {
            map.put("papi", true);
        }
        if (kether) {
            map.put("kether", true);
        }
        return map;
    }

}
