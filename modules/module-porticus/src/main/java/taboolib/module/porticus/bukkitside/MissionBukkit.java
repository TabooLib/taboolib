package taboolib.module.porticus.bukkitside;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import taboolib.common.platform.function.IOKt;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.MessageBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * Porticus
 * taboolib.module.porticus.bukkitside.MissionBukkit
 *
 * @author bkm016
 * @since 2020/10/15 10:03 下午
 */
public class MissionBukkit extends PorticusMission {

    private final Plugin plugin = JavaPlugin.getProvidingPlugin(Porticus.class);

    public MissionBukkit() {
    }

    public MissionBukkit(UUID uid) {
        super(uid);
    }

    @Override
    public void run(@NotNull Object target) {
        super.run(target);
        sendBukkitMessage((Player) target, command);
    }

    public void sendBukkitMessage(Player player, String[] command) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                for (byte[] bytes : MessageBuilder.create(command)) {
                    player.sendPluginMessage(plugin, "porticus_" + IOKt.getPluginId().toLowerCase() + ":main", bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
