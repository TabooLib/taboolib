package io.izzel.taboolib.module.command.commodore;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.BaseSubCommand;
import org.bukkit.plugin.Plugin;


import java.lang.reflect.Field;

public class TCommodoreHandler {
    /**
     * 为所有子命令注册高亮支持
     *
     * @param baseMainCommand 需要注册的命令
     */
    public static void registerSubCommands(Plugin plugin,BaseMainCommand baseMainCommand) {
        // 检查是否支持
        if(!CommodoreProvider.isSupported()){
            return;
        }

        // 注册子命令
        LiteralArgumentBuilder<Object> literal = LiteralArgumentBuilder.literal(baseMainCommand.getRegisterCommand().getName());
        for (BaseSubCommand subCommand : baseMainCommand.getSubCommands()) {
            literal.then(LiteralArgumentBuilder.literal(subCommand.getLabel()));
            if(subCommand.getAliases().length != 0){
                for (String subCommandAlias : subCommand.getAliases()) {
                    literal.then(LiteralArgumentBuilder.literal(subCommandAlias));

                }
            }
        }
        for (String alias : baseMainCommand.getRegisterCommand().getAliases()) {
            LiteralArgumentBuilder<Object> literalA = LiteralArgumentBuilder.literal(alias);
            for (BaseSubCommand subCommand : baseMainCommand.getSubCommands()) {
                literalA.then(LiteralArgumentBuilder.literal(subCommand.getLabel()));
                if (subCommand.getAliases().length != 0) {
                    for (String subCommandAlias : subCommand.getAliases()) {
                        literalA.then(LiteralArgumentBuilder.literal(subCommandAlias));
                    }
                }
            }
        }
        CommodoreProvider.getCommodore(plugin).register(baseMainCommand.getRegisterCommand(),literal);
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
