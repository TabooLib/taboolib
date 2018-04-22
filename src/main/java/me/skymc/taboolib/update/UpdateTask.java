package me.skymc.taboolib.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.message.MsgUtils;

/**
 * @author sky
 * @since 2018年2月23日 下午10:39:14
 */
public class UpdateTask {
	
	/**
	 * 检测更新
	 */
	public UpdateTask() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (!Main.getInst().getConfig().getBoolean("UPDATE-CHECK")) {
					return;
				}
				String value = FileUtils.getStringFromURL("https://github.com/Bkm016/TabooLib/releases", 1024);
				if (value == null) {
					return;
				}
				Pattern pattern = Pattern.compile("<a href=\"/Bkm016/TabooLib/releases/tag/(\\S+)\">");
				Matcher matcher = pattern.matcher(value);
				if (matcher.find()) {
					double newVersion = Double.valueOf(matcher.group(1));
					if (TabooLib.getPluginVersion() >= newVersion) {
						MsgUtils.send("插件已是最新版, 无需更新!");
					}
					else {
						MsgUtils.send("&8####################################################");
						MsgUtils.send("检测到有新的版本更新!");
						MsgUtils.send("当前版本: &f" + TabooLib.getPluginVersion());
						MsgUtils.send("最新版本: &f" + newVersion);
						MsgUtils.send("下载地址: &fhttp://www.mcbbs.net/thread-773065-1-1.html");
						MsgUtils.send("开源地址: &fhttps://github.com/Bkm016/TabooLib/");
						MsgUtils.send("&8####################################################");
					}
				}
			}
		}.runTaskLaterAsynchronously(Main.getInst(), 100);
	}
}
