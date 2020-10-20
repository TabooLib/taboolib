package io.izzel.taboolib.module.command.commodore;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.izzel.taboolib.module.command.base.Argument;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import org.bukkit.plugin.Plugin;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCommodoreHandler {
    /**
     * 为所有子命令注册高亮支持
     *
     * @param baseMainCommand 需要注册的命令
     */
    @SuppressWarnings("rawtypes")
    public static void registerSubCommands(Plugin plugin, BaseMainCommand baseMainCommand) {
        // 检查是否支持
        if(!CommodoreProvider.isSupported()){
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
            CommodoreProvider.getCommodore(plugin).register(baseMainCommand.getRegisterCommand(),literal);
        }
    }


    /**
     * 将带有NeedCommodore的命令进行高亮注册
     * <p>如不含NeedCommodore,自动注册SubCommand</p>
     *
     * @param clazz 插件命令的类
     * @param plugin 注册的插件
     * @param mainCommand 命令的实例
     */
    public static void handle(Class<?> clazz, Plugin plugin, BaseMainCommand mainCommand) {
        boolean flag = false;
        for (Field declaredField : clazz.getDeclaredFields()) {
            // 跳过不含注解的类
            if(declaredField.getAnnotation(NeedCommodore.class) == null){
                continue;
            }
            //region 处理注解
            declaredField.setAccessible(true);
            try {
                // 判断类型
                if(declaredField.getType() != LiteralArgumentBuilder.class){
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
        if(!flag){
            registerSubCommands(plugin,mainCommand);
        }
    }
}
