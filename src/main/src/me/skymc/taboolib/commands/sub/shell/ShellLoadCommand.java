package me.skymc.taboolib.commands.sub.shell;

import java.io.File;

import org.bukkit.command.CommandSender;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.javashell.JavaShell;
import me.skymc.taboolib.message.MsgUtils;

public class ShellLoadCommand extends SubCommand {

	public ShellLoadCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length < 3) {
			MsgUtils.send(sender, "&c请输入正确的脚本名称");
			return;
		}
		
		File file = new File(JavaShell.getScriptFolder(), args[2].contains(".java") ? args[2] : args[2] + ".java");
		if (!file.exists()) {
			MsgUtils.send(sender, "&c脚本 &4" + args[2] + "&c 不存在");
			return;
		}
		
		if (JavaShell.reloadShell(args[2])) {
			MsgUtils.send(sender, "脚本 " + args[2] + " 已载入");
		}
	}

	@Override
	public boolean command() {
		return true;
	}

}
