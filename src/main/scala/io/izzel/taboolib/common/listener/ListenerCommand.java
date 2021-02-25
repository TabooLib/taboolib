package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.common.loader.Startup;
import io.izzel.taboolib.common.loader.StartupLoader;
import io.izzel.taboolib.module.command.lite.CommandBuilder;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Files;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.net.ConnectException;

/**
 * @author sky
 */
@TListener
public class ListenerCommand implements Listener {

    static {
        StartupLoader.register(ListenerCommand.class);
    }

    @Startup.Starting
    void init() {
        // 版本命令
        CommandBuilder.create("taboolib", TabooLib.getPlugin())
                .aliases("lib")
                .execute((sender, args) -> {
                    sender.sendMessage("§8[§fTabooLib§8] §7Currently Version: §fv" + TabooLib.getVersion());
                    sender.sendMessage("§8[§fTabooLib§8] §7Boot: §f" + PluginLoader.getFirstLoaded());
                }).build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void e(ServerCommandEvent e) {
        if (e.getCommand().equalsIgnoreCase("saveFiles")) {
            Local.saveFiles();
            LocalPlayer.saveFiles();
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("libUpdate")) {
            e.setCancelled(true);
            e.getSender().sendMessage("§8[§fTabooLib§8] §cWARNING §7| §4Update TabooLib will force to restart your server. Please confirm this action by type §c/libupdateconfirm");
        } else if (e.getCommand().equalsIgnoreCase("libUpdateConfirm") || e.getCommand().equalsIgnoreCase("libUpdate confirm")) {
            e.getSender().sendMessage("§8[§fTabooLib§8] §7Downloading TabooLib file...");
            try {
                Files.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar", new File("libs/TabooLib.jar"));
                e.getSender().sendMessage("§8[§fTabooLib§8] §2Download completed, the server will restart in 3 secs");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Bukkit.shutdown();
            } catch (ConnectException t) {
                e.getSender().sendMessage("§8[§fTabooLib§8] §2Download failed.");
            }
        }
    }
}
