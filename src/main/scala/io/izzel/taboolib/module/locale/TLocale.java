package io.izzel.taboolib.module.locale;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.compat.PlaceholderHook;
import io.izzel.taboolib.module.locale.chatcolor.TColor;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语言文件工具
 *
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

    @NotNull
    public static String asString(String path, Object... args) {
        return asString0(path, toArray(args));
    }

    @NotNull
    public static String asString(String path, String... args) {
        return asString0(path, args);
    }

    @NotNull
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

    /**
     * 语言文件扩展 Tellraw 信息工具
     */
    public static final class Tellraw extends TLocale {

        /**
         * 发送 Tellraw 信息
         *
         * @param sender     目标
         * @param rawMessage 信息
         */
        public static void send(CommandSender sender, String rawMessage) {
            if (sender instanceof Player) {
                TellrawCreator.getAbstractTellraw().sendRawMessage((Player) sender, rawMessage);
            } else {
                sender.sendMessage(TextComponent.toLegacyText(ComponentSerializer.parse(rawMessage)));
            }
        }
    }

    /**
     * 语言文件扩展展示工具
     */
    public static final class Display extends TLocale {

        /**
         * 发送标题
         * 默认为 10 淡入 20 停留 10 淡出
         *
         * @param player   玩家
         * @param title    大标题
         * @param subTitle 小标题
         */
        public static void sendTitle(Player player, String title, String subTitle) {
            sendTitle(player, title, subTitle, 10, 20, 10);
        }

        /**
         * 发送标题
         *
         * @param player   玩家
         * @param title    大标题
         * @param subTitle 小标题
         * @param fadein   淡入
         * @param stay     停留
         * @param fadeout  淡出
         */
        public static void sendTitle(Player player, String title, String subTitle, int fadein, int stay, int fadeout) {
            NMS.handle().sendTitle(player, title, fadein, stay, fadeout, subTitle, fadein, stay, fadeout);
        }

        /**
         * 发送动作栏信息
         *
         * @param player 玩家
         * @param text   信息
         */
        public static void sendActionBar(Player player, String text) {
            NMS.handle().sendActionBar(player, text);
        }
    }

    /**
     * 语言文件扩展转换工具
     */
    public static final class Translate extends TLocale {

        /**
         * 是否启用 PlaceholderAPI 支持
         */
        public static boolean isPlaceholderUseDefault() {
            return TabooLib.getConfig().getBoolean("LOCALE.USE_PAPI", false);
        }

        /**
         * 检查 PlaceholderAPI 插件是否启用
         */
        public static boolean isPlaceholderPluginEnabled() {
            return PlaceholderHook.isHooked();
        }

        /**
         * 设置颜色，使用 '&' 作为颜色符号
         *
         * @param args 文本
         */
        @NotNull
        public static String setColored(@NotNull String args) {
            return TColor.translate(args);
        }

        /**
         * 设置颜色，使用 '&' 作为颜色符号
         *
         * @param args 文本
         */
        @NotNull
        public static List<String> setColored(@NotNull List<String> args) {
            List<String> colored = new ArrayList<>();
            for (String a : args) {
                colored.add(TColor.translate(a));
            }
            return colored;
        }

        /**
         * 移除颜色
         *
         * @param args 文本
         */
        @NotNull
        public static String setUncolored(@NotNull String args) {
            return ChatColor.stripColor(args);
        }

        /**
         * 移除颜色
         *
         * @param args 文本
         */
        @NotNull
        public static List<String> setUncolored(@NotNull List<String> args) {
            return args.stream().map(ChatColor::stripColor).collect(Collectors.toList());
        }

        /**
         * 进行 PlaceholderAPI 变量转换
         *
         * @param sender 用户
         * @param args   文本
         */
        @NotNull
        public static String setPlaceholders(@NotNull CommandSender sender, @NotNull String args) {
            return PlaceholderHook.replace(sender, args);
        }

        /**
         * 进行 PlaceholderAPI 变量转换
         *
         * @param sender 用户
         * @param args   文本
         */
        @NotNull
        public static List<String> setPlaceholders(CommandSender sender, @NotNull List<String> args) {
            return args.stream().map(var -> PlaceholderHook.replace(sender, var)).collect(Collectors.toList());
        }
    }

    /**
     * 语言文件扩展日志工具
     */
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
