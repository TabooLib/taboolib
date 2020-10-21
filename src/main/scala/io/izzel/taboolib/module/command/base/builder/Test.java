package io.izzel.taboolib.module.command.base.builder;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.command.base.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

/**
 * TabooLib
 * io.izzel.taboolib.module.command.base.builder.Test
 *
 * 2020/10/22 1:10 测试结果：
 * restrict生效，补全失效，hover失效
 *
 * @author bkm016
 * @since 2020/10/22 12:47 上午
 */
@BaseCommand(name = "example")
public class Test extends BaseMainCommand {

    @SubCommand
    final BaseSubCommand itemstack = new BaseSubCommand() {

        @Override
        public Argument[] getArguments() {
            return of(
                    new Argument("物品")
                            .restrict(ArgumentType.bukkit(ArgumentType.Bukkit.ITEM_STACK))
                            .complete(new ArrayList<>()),
                    new Argument("玩家")
                            .optional()
                            .restrict(ArgumentType.bukkit(ArgumentType.Bukkit.ENTITY))
                            .complete(new ArrayList<>())
            );
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {

        }
    };

    @SubCommand
    final BaseSubCommand gamemode = new BaseSubCommand() {

        @Override
        public Argument[] getArguments() {
            return of(
                    new Argument("模式")
                            .restrict(ArgumentType.integer(0, 3))
                            .complete(Lists.newArrayList("0", "1", "2", "3")),
                    new Argument("玩家")
                            .optional()
                            .restrict(ArgumentType.bukkit(ArgumentType.Bukkit.ENTITY))
                            .complete(new ArrayList<>())
            );
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String label, String[] args) {

        }
    };
}
