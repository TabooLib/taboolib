package io.izzel.taboolib.test;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.izzel.taboolib.module.command.base.*;
import io.izzel.taboolib.module.command.commodore.CustomCommodore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@BaseCommand(name = "test")
public class CommandTest extends BaseMainCommand {

    @SubCommand
    final BaseSubCommand command = new BaseSubCommand() {

        @Override
        public Argument[] getArguments() {
            return of(
                    new Argument("args-1")
                            .optional()
                            .restrict(ArgumentType.doubleArg(100, 200)),
                    new Argument("args-2")
                            .optional()
                            .complete(Arrays.asList("1", "2", "3"))
            );
        }

        @Override
        public void onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        }
    };

    @CustomCommodore
    final LiteralArgumentBuilder<?> builder = LiteralArgumentBuilder.literal("test")
            .then(LiteralArgumentBuilder.literal("help"))
            .then(LiteralArgumentBuilder.literal("test")
                    .then(RequiredArgumentBuilder.argument("string", StringArgumentType.word())
                            .then(RequiredArgumentBuilder.argument("string", StringArgumentType.string())
                                    .then(RequiredArgumentBuilder.argument("string3", StringArgumentType.greedyString())
                                    )
                            )
                    )
            )
            .then(LiteralArgumentBuilder.literal("qawq")
                    .then(RequiredArgumentBuilder.argument("int", IntegerArgumentType.integer())
                            .then(RequiredArgumentBuilder.argument("bool", BoolArgumentType.bool())
                            )
                    )
            );


    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String argument) {
        if (command.equals("command") && argument.equals("param1")) {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        }
        return null;
    }

    @SubCommand(description = "description", arguments = {"param1", "param2"})
    public void command(CommandSender sender, String[] args) {

    }
}
