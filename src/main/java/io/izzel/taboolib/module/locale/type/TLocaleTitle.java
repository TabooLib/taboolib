package io.izzel.taboolib.module.locale.type;

import com.google.common.collect.Maps;
import io.izzel.taboolib.kotlin.kether.KetherFunction;
import io.izzel.taboolib.kotlin.kether.ScriptContext;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleSerialize;
import io.izzel.taboolib.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Bkm016
 * @since 2018-04-22
 */

@Immutable
@SerializableAs("TITLE")
public class TLocaleTitle extends TLocaleSerialize {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int fadeOut;
    private final int stay;

    private TLocaleTitle(String title, String subString, int fadeIn, int fadeOut, int stay, boolean papi, boolean kether) {
        super(papi, kether);
        this.title = title;
        this.subtitle = subString;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.stay = stay;
    }

    public static TLocaleTitle valueOf(Map<String, Object> map) {
        TLocaleTitle title;
        try {
            title = new TLocaleTitle(
                    getStringOrDefault(map, "title", ""),
                    getStringOrDefault(map, "subtitle", ""),
                    getIntegerOrDefault(map, "fadein", 10),
                    getIntegerOrDefault(map, "fadeout", 10),
                    getIntegerOrDefault(map, "stay", 10),
                    isPlaceholderEnabled(map),
                    isKetherEnabled(map));
        } catch (Exception e) {
            title = new TLocaleTitle("Empty Title message.", e.getMessage(), 10, 20, 10, false, false);
        }
        return title;
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            TLocale.Display.sendTitle((Player) sender, replaceText(sender, Strings.replaceWithOrder(title, args)), replaceText(sender, Strings.replaceWithOrder(subtitle, args)), fadeIn, stay, fadeOut);
        } else {
            TLocale.Logger.error("LOCALE.TITLE-SEND-TO-NON-PLAYER", asString(args));
        }
    }

    @Override
    public String asString(String... args) {
        return Strings.replaceWithOrder(Strings.replaceWithOrder("TITLE: [title: ''{0}'', subtitle: ''{1}'', fadeIn: {2}, fadeOut: {3}]", title, subtitle, fadeIn, fadeOut), args);
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("title", title);
        map.put("subtitle", subtitle);
        map.put("fadein", fadeIn);
        map.put("fadeout", fadeOut);
        map.put("stay", stay);
        if (papi) {
            map.put("papi", true);
        }
        if (kether) {
            map.put("kether", true);
        }
        return map;
    }

    private String replaceText(CommandSender sender, String text, String... args) {
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
}
