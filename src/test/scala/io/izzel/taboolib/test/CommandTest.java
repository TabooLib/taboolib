package io.izzel.taboolib.test;

import io.izzel.taboolib.module.command.base.BaseCommand;
import io.izzel.taboolib.module.command.base.BaseMainCommand;
import io.izzel.taboolib.module.command.base.SubCommand;
import io.izzel.taboolib.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import java.util.List;
import java.util.stream.Collectors;

@BaseCommand(name = "test")
public class CommandTest extends BaseMainCommand {

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
