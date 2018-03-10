package me.skymc.taboolib.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import pw.yumc.Yum.events.PluginNetworkEvent;

/**
 * @author sky
 * @since 2018年2月23日 下午11:10:03
 */
public class ListenerNetWork implements Listener {
	
	public static final String GG = "本监听只是为了防止本插件的更新检测被 YUM 插件阻止，别无它用。";
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNetWork(PluginNetworkEvent e) {
		if (e.getPlugin() != null && e.getPlugin().equals(Main.getInst())) {
			// 取消阻止
			e.setCancelled(false);
			// 后台提示
			MsgUtils.warn("已取消 &4YUM &c对本插件网络访问的阻止!");
		}
	}
}
