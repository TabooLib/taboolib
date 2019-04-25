package com.ilummc.tlib.resources;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.bungee.api.ChatColor;
import com.ilummc.tlib.bungee.api.chat.TextComponent;
import com.ilummc.tlib.bungee.chat.ComponentSerializer;
import com.ilummc.tlib.inject.TLoggerManager;
import com.ilummc.tlib.util.Ref;
import com.ilummc.tlib.util.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.common.nms.NMSHandler;
import me.skymc.taboolib.json.tellraw.TellrawCreator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    static String asString(String path, Class<?> callerClass, String... args) {
        return TLocaleLoader.asString(Ref.getCallerPlugin(callerClass), path, args);
    }

    static List<String> asStringList(String path, Class<?> callerClass, String... args) {
        return TLocaleLoader.asStringList(Ref.getCallerPlugin(callerClass), path, args);
    }

    private static void sendTo(String path, CommandSender sender, String[] args, Class<?> callerClass) {
        TLocaleLoader.sendTo(Ref.getCallerPlugin(callerClass), path, sender, args);
    }

    public static void sendToConsole(String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, Bukkit.getConsoleSender(), args, clazz));
    }

    public static void sendTo(CommandSender sender, String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static void sendTo(String path, CommandSender sender, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static void broadcast(String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> Bukkit.getOnlinePlayers().forEach(player -> sendTo(path, player, args, clazz)));
    }

    public static String asString(String path, String... args) {
        try {
            return asString(path, Ref.getCallerClass(3).orElse(Main.class), args);
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("FETCH-LOCALE-ERROR"), path));
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return "§4<" + path + "§4>";
        }
    }

    public static List<String> asStringList(String path, String... args) {
        try {
            return asStringList(path, Ref.getCallerClass(3).orElse(Main.class), args);
        } catch (Exception e) {
            TLib.getTLib().getLogger().error(Strings.replaceWithOrder(TLib.getInternalLanguage().getString("LOCALE-ERROR-REASON"), e.getMessage()));
            return Collections.singletonList("§4<" + path + "§4>");
        }
    }

    public static void reload() {
        Ref.getCallerClass(3).ifPresent(clazz -> TLocaleLoader.load(Ref.getCallerPlugin(clazz), false));
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
            NMSHandler.getHandler().sendTitle(player, title, fadein, stay, fadeout, subTitle, fadein, stay, fadeout);
        }

        public static void sendActionBar(Player player, String text) {
            NMSHandler.getHandler().sendActionBar(player, text);
        }
    }

    public static final class Translate extends TLocale {

        public static boolean isPlaceholderUseDefault() {
            return Main.getInst().getConfig().getBoolean("LOCALE.USE_PAPI", false);
        }

        public static boolean isPlaceholderPluginEnabled() {
            return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
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
            return isPlaceholderPluginEnabled() ? sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, args) : args : args;
        }

        public static List<String> setPlaceholders(CommandSender sender, List<String> args) {
            return isPlaceholderPluginEnabled() ? sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, args) : args : args;
        }
    }

    public static final class Logger extends TLocale {

        public static void info(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).info(locale)));
        }

        public static void warn(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).warn(locale)));
        }

        public static void error(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).error(locale)));
        }

        public static void fatal(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).fatal(locale)));
        }

        public static void fine(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).fine(locale)));
        }

        public static void finest(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).finest(locale)));
        }

        public static void verbose(String path, String... args) {
            Ref.getCallerClass(3).ifPresent(clazz -> asStringList(path, clazz, args).forEach(locale -> TLoggerManager.getLogger(Ref.getCallerPlugin(clazz)).verbose(locale)));
        }
    }
}
