package me.skymc.tlm.command.sub;

import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.tlm.TLM;
import me.skymc.tlm.module.ITabooLibraryModule;
import me.skymc.tlm.module.TabooLibraryModule;
import org.bukkit.command.CommandSender;

/**
 * @author sky
 * @since 2018年2月18日 下午2:09:34
 */
public class TLMReloadCommand extends SubCommand {

	/**
	 * @param sender
	 * @param args
	 */
	public TLMReloadCommand(CommandSender sender, String[] args) {
		super(sender, args);
		if (args.length != 2) {
			MsgUtils.send(sender, "&4参数错误。");
		}
		
		else if ("tlm".equalsIgnoreCase(args[1])) {
			TLM.getInst().reloadConfig();
			MsgUtils.send(sender, "&fTLM &7配置文件已重载。");
		}
		
		else if ("all".equalsIgnoreCase(args[1])) {
			TabooLibraryModule.getInst().reloadConfig();
			MsgUtils.send(sender, "所有模块配置文件已重载。");
		}
		
		else {
			ITabooLibraryModule module = TabooLibraryModule.getInst().valueOf(args[1]);
			if (module == null) {
				MsgUtils.send(sender, "&4模块 &c" + args[1] + " &4不存在。");
			}
			else {
				TabooLibraryModule.getInst().reloadConfig(module, true);
				MsgUtils.send(sender, "模块 &f" + args[1] + " &7的配置文件已重载。");
			}
		}
	}

}
