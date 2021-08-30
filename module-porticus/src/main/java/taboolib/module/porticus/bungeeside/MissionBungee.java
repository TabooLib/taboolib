package taboolib.module.porticus.bungeeside;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import taboolib.common.platform.function.IOKt;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.MessageBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * Porticus
 * taboolib.module.porticus.bungeeside.MissionBungee
 *
 * @author bkm016
 * @since 2020/10/15 10:06 下午
 */
public class MissionBungee extends PorticusMission {

    private static final Plugin plugin = BungeeCord.getInstance().pluginManager.getPlugins().iterator().next();

    public MissionBungee() {
        super();
    }

    public MissionBungee(UUID uid) {
        super(uid);
    }

    @Override
    public void run(@NotNull Object target) {
        super.run(target);
        if (target instanceof Server) {
            sendBungeeMessage((Server) target, command);
        } else if (target instanceof ProxiedPlayer) {
            sendBungeeMessage((ProxiedPlayer) target, command);
        }
    }

    public static void sendBungeeMessage(ProxiedPlayer player, String... args) {
        sendBungeeMessage(player.getServer(), args);
    }

    public static void sendBungeeMessage(Server server, String... args) {
        BungeeCord.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                for (byte[] bytes : MessageBuilder.create(args)) {
                    server.sendData("porticus_" + IOKt.getPluginId().toLowerCase() + ":main", bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
