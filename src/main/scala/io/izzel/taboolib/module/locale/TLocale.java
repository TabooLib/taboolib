package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import io.izzel.taboolib.module.locale.logger.TLoggerManager;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.tellraw.TellrawCreator;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.chat.ChatColor;
import io.izzel.taboolib.util.chat.ComponentSerializer;
import io.izzel.taboolib.util.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzzelAliz
 */
public class TLocale {

    private TLocale() {
        throw new AssertionError();
    }

    static String[] toArray(Object... obj) {
        return Arrays.stream(obj).map(String::valueOf).toArray(String[]::new);
    }

    static void sendTo0(Collection<? extends CommandSender> sender, String path, String... args) {
        sender.forEach(i -> TLocaleLoader.sendTo(Ref.getCallerPlugin(), path, i, args));
    }

    static String asString0(String path, String... args) {
        try {
            return TLocaleLoader.asString(Ref.getCallerPlugin(), path, args);
        } catch (Exception e) {
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("FETCH-LOCALE-ERROR"), path));
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return "§4Error: " + path;
        }
    }

    static List<String> asStringList0(String path, String... args) {
        try {
            return TLocaleLoader.asStringList(Ref.getCallerPlugin(), path, args);
        } catch (Exception e) {
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("FETCH-LOCALE-ERROR"), path));
            TabooLib.getLogger().error(Strings.replaceWithOrder(TabooLib.getInst().getInternal().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return Collections.singletonList("§4Error: " + path);
        }
    }

    public static void sendTo(CommandSender sender, String path, Object... args) {
        sendTo0(Collections.singletonList(sender), path, toArray(args));
    }

    public static void sendTo(CommandSender sender, String path, String... args) {
        sendTo0(Collections.singletonList(sender), path, args);
    }

    public static void sendTo(CommandSender sender, String path) {
        sendTo0(Collections.singletonList(sender), path);
    }

    public static void sendToConsole(String path, Object... args) {
        sendTo0(Collections.singletonList(Bukkit.getConsoleSender()), path, toArray(args));
    }

    public static void sendToConsole(String path, String... args) {
        sendTo0(Collections.singletonList(Bukkit.getConsoleSender()), path, args);
    }

    public static void sendToConsole(String path) {
        sendTo0(Collections.singletonList(Bukkit.getConsoleSender()), path);
    }

    public static void broadcast(String path, Object... args) {
        sendTo0(Bukkit.getOnlinePlayers(), path, toArray(args));
    }

    public static void broadcast(String path, String... args) {
        sendTo0(Bukkit.getOnlinePlayers(), path, args);
    }

    public static void broadcast(String path) {
        sendTo0(Bukkit.getOnlinePlayers(), path);
    }

    public static String asString(String path, Object... args) {
        return asString0(path, toArray(args));
    }

    public static String asString(String path, String... args) {
        return asString0(path, args);
    }

    public static String asString(String path) {
        return asString0(path);
    }

    public static List<String> asStringList(String path, Object... args) {
        return asStringList0(path, toArray(args));
    }

    public static List<String> asStringList(String path, String... args) {
        return asStringList0(path, args);
    }

    public static List<String> asStringList(String path) {
        return asStringList0(path);
    }

    public static void reload() {
        Ref.getCallerClass(3).ifPresent(clazz -> TLocaleLoader.load(Ref.getCallerPlugin(clazz), true));
    }

    public static final class Tellraw extends TLocale {

        public static void send(CommandSender sender, String rawMessage) {
            if (sender instanceof Player) {
                TellrawCreator.getAbstractTellraw().sendRawMessage((Player) sender, rawMessage);
            } else {
                sender.sendMessage(TextComponent.toLegacyText(ComponentSerializer.parse(rawMessage)));
            }
        }
    }

    public static final class Display extends TLocale {

        public static void sendTitle(Player player, String title, String subTitle) {
            sendTitle(player, title, subTitle, 10, 20, 10);
        }

        public static void sendTitle(Player player, String title, String subTitle, int fadein, int stay, int fadeout) {
            NMS.handle().sendTitle(player, title, fadein, stay, fadeout, subTitle, fadein, stay, fadeout);
        }

        public static void sendActionBar(Player player, String text) {
            NMS.handle().sendActionBar(player, text);
        }
    }

    public static final class Translate extends TLocale {

        public static boolean isPlaceholderUseDefault() {
            return TabooLib.getConfig().getBoolean("LOCALE.USE_PAPI", false);
        }

        public static boolean isPlaceholderPluginEnabled() {
            return PlaceholderHook.isHooked();
        }

        public static String setColored(String args) {
            return ChatColor.translateAlternateColorCodes('&', args);
        }

        public static List<String> setColored(List<String> args) {
            return args.stream().map(var -> ChatColor.translateAlternateColorCodes('&', var)).collect(Collectors.toList());
        }

        public static String setUncolored(String args) {
            return ChatColor.stripColor(args);
        }

        public static List<String> setUncolored(List<String> args) {
            return args.stream().map(ChatColor::stripColor).collect(Collectors.toList());
        }

        public static String setPlaceholders(CommandSender sender, String args) {
            return PlaceholderHook.replace(sender, args);
        }

        public static List<String> setPlaceholders(CommandSender sender, List<String> args) {
            return args.stream().map(var -> PlaceholderHook.replace(sender, var)).collect(Collectors.toList());
        }
    }

    public static final class Logger extends TLocale {

        public static void info(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).info(locale));
        }

        public static void warn(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).warn(locale));
        }

        public static void error(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).error(locale));
        }

        public static void fatal(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).fatal(locale));
        }

        public static void fine(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).fine(locale));
        }

        public static void finest(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).finest(locale));
        }

        public static void verbose(String path, String... args) {
            asStringList(path, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin()).verbose(locale));
        }
    }
}
