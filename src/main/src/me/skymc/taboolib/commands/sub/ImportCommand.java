package me.skymc.taboolib.commands.sub;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.message.MsgUtils;

public class ImportCommand extends SubCommand {

	public ImportCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (isPlayer()) {
			MsgUtils.warn("改命令只能由控制台输入");
		}
		else if (Main.getStorageType() == StorageType.LOCAL) {
			MsgUtils.warn("只有启用数据库储存时才能这么做");
		}
		else {
			MsgUtils.send("正在清空数据库...");
			Main.getConnection().truncateTable(Main.getTablePrefix() + "_playerdata");
			
			MsgUtils.send("开始导入玩家数据...");
			int size = Main.getPlayerDataFolder().listFiles().length;
			int loop = 1;
			
			for (File file : Main.getPlayerDataFolder().listFiles()) {
				FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
				Main.getConnection().intoValue(Main.getTablePrefix() + "_playerdata", file.getName().replace(".yml", ""), ConfigUtils.encodeYAML(conf));
				
				MsgUtils.send("导入玩家: &f" + file.getName().replace(".yml", "") + " &7进度: &f" + loop + "/" + size);
				loop++;
			}
			MsgUtils.send("导入完成!");
		}
	}

}
