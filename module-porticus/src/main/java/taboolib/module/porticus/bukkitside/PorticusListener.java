package taboolib.module.porticus.bukkitside;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import taboolib.common.platform.function.IOKt;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.Message;
import taboolib.module.porticus.common.MessageReader;

import java.io.IOException;

/**
 * @author 坏黑
 * @since 2020-10-15
 */
public class PorticusListener implements Listener, PluginMessageListener {

    public PorticusListener() {
        Plugin plugin = JavaPlugin.getProvidingPlugin(Porticus.class);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "porticus_" + IOKt.getPluginId().toLowerCase() + ":main", this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "porticus_" + IOKt.getPluginId().toLowerCase() + ":main");
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PorticusMission mission : Porticus.INSTANCE.getMissions()) {
                if (mission.isTimeout()) {
                    if (mission.getTimeoutRunnable() != null) {
                        try {
                            mission.getTimeoutRunnable().run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    Porticus.INSTANCE.getMissions().remove(mission);
                }
            }
        }, 0, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PorticusBukkitEvent e) {
        for (PorticusMission mission : Porticus.INSTANCE.getMissions()) {
            if (mission.getUID().equals(e.getUID())) {
                if (mission.getResponseConsumer() != null) {
                    try {
                        mission.getResponseConsumer().accept(e.getArgs());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                Porticus.INSTANCE.getMissions().remove(mission);
            }
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte [] bytes) {
        if (channel.equalsIgnoreCase("porticus_" + IOKt.getPluginId().toLowerCase() + ":main")) {
            try {
                Message message = MessageReader.read(bytes);
                if (message.isCompleted()) {
                    PorticusBukkitEvent.call(player, message.getMessages().get(0).getUID(), message.build());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
