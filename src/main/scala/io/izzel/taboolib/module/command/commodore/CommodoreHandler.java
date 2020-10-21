package io.izzel.taboolib.module.command.commodore;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.ArgumentType;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 为 {@link BaseMainCommand} 提供子命令参数高亮注册
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CommodoreHandler {

    public static void register(BaseMainCommand baseMainCommand) {
        if (CommodoreProvider.isSupported()) {
            // 注册主命令
            register(baseMainCommand.getRegisterCommand().getName(), baseMainCommand);
            // 注册别名
            for (String aliases : baseMainCommand.getRegisterCommand().getAliases()) {
                register(aliases, baseMainCommand);
            }
        }
    }

    private static void register(String name, BaseMainCommand baseMainCommand) {
        LiteralArgumentBuilder builder = LiteralArgumentBuilder.literal(name);
        for (BaseSubCommand subCommand : baseMainCommand.getSubCommands()) {
            // 注册子命令
            register(builder, subCommand.getLabel(), subCommand.getArguments());
            // 注册别名
            for (String alias : subCommand.getAliases()) {
                register(builder, alias, subCommand.getArguments());
            }
        }
        // 注册 Brigadier
        CommodoreProvider.getCommodore(baseMainCommand.getRegisterCommand().getPlugin()).register(baseMainCommand.getRegisterCommand(), builder);
    }

    private static void register(LiteralArgumentBuilder builder, String name, Argument[] arguments) {
        if (arguments.length > 0) {
            builder.then(LiteralArgumentBuilder.literal(name).then(argument(arguments, 0)));
        }
    }

    private static ArgumentBuilder argument(Argument[] arguments, int index) {
        Object nms = Optional.ofNullable(arguments[index].getRestrict()).orElse(ArgumentType.word()).toNMS();
        if (index + 1 < arguments.length) {
            return RequiredArgumentBuilder.argument(arguments[index].getName(), (com.mojang.brigadier.arguments.ArgumentType) nms).then(argument(arguments, index + 1));
        } else {
            return RequiredArgumentBuilder.argument(arguments[index].getName(), (com.mojang.brigadier.arguments.ArgumentType) nms);
        }
    }

    @Deprecated
    public static void registerSubCommands(Plugin plugin, BaseMainCommand baseMainCommand) {
        if (!CommodoreProvider.isSupported()) {
            return;
        }
        // 注册子命令
        List<String> subStrings = new ArrayList<>(baseMainCommand.getRegisterCommand().getAliases());
        subStrings.add(baseMainCommand.getRegisterCommand().getName());
        for (String alias : subStrings) {
            LiteralArgumentBuilder<Object> literal = LiteralArgumentBuilder.literal(alias);
            // subCommands
            for (BaseSubCommand subCommand : baseMainCommand.getSubCommands()) {
                // register subAliases
                List<String> cmd = new ArrayList<>(Arrays.asList(subCommand.getAliases()));
                cmd.add(subCommand.getLabel());
                for (String subCommandAlias : cmd) {
                    CommandNode current = LiteralArgumentBuilder.literal(subCommandAlias).build();
                    literal.then(current);
                    for (Argument argument : subCommand.getArguments()) {
                        CommandNode temp = RequiredArgumentBuilder.argument(argument.getName(), StringArgumentType.word()).build();
                        current.addChild(temp);
                        current = temp;
                    }
                }
            }
            CommodoreProvider.getCommodore(plugin).register(baseMainCommand.getRegisterCommand(), literal);
        }
    }


    @Deprecated
    public static void handle(Class<?> clazz, Plugin plugin, BaseMainCommand mainCommand) {
        if (!CommodoreProvider.isSupported()) {
            return;
        }
        boolean flag = false;
        for (Field declaredField : clazz.getDeclaredFields()) {
            // 跳过不含注解的类
            if (declaredField.getAnnotation(CustomCommodore.class) == null) {
                continue;
            }
            //region 处理注解
            declaredField.setAccessible(true);
            try {
                // 判断类型
                if (declaredField.getType() != LiteralArgumentBuilder.class) {
                    continue;
                }
                LiteralArgumentBuilder<?> builder = (LiteralArgumentBuilder<?>) declaredField.get(mainCommand);
                // 注册Commodore
                CommodoreProvider.getCommodore(plugin).register(builder);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            //endregion
            flag = true;
            break;
        }
        if (!flag) {
            registerSubCommands(plugin, mainCommand);
        }
    }
}
