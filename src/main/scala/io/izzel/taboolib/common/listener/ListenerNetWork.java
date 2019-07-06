package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pw.yumc.Yum.events.PluginNetworkEvent;

/**
 * @author sky
 * @since 2018年2月23日 下午11:10:03
 */
@TListener(depend = "YUM")
public class ListenerNetWork implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNetWork(PluginNetworkEvent e) {
        if (e.getPlugin() != null && e.getPlugin().getName().equals("TabooLib")) {
            e.setCancelled(false);
        }
    }
}
