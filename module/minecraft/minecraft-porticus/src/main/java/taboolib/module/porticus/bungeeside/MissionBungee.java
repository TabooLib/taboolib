package taboolib.module.porticus.bungeeside;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.MessageBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Porticus
 * taboolib.module.porticus.bungeeside.MissionBungee
 *
 * @author bkm016
 * @since 2020/10/15 10:06 下午
 */
public class MissionBungee extends PorticusMission {

    public MissionBungee() {
        super();
    }

    public MissionBungee(UUID uid) {
        super(uid);
    }

    @Override
    public void run(@NotNull Object target) {
        if (command == null) {
            throw new IllegalStateException("command must be set before running mission");
        }
        boolean tracked = consumer != null || runnable != null;
        MessageTarget messageTarget = resolveTarget(target, tracked);
        Plugin plugin = getPlugin();
        List<byte[]> messages;
        try {
            messages = MessageBuilder.create(command);
        } catch (IOException e) {
            throw new IllegalStateException("failed to encode mission command", e);
        }
        super.run(target);
        try {
            ScheduledTask task = BungeeCord.getInstance().getScheduler().runAsync(plugin, () -> {
                if (tracked && !isPending()) {
                    return;
                }
                try {
                    sendMessages(messageTarget, messages, true);
                } catch (Throwable t) {
                    Porticus.INSTANCE.getMissions().remove(getUID(), this);
                    t.printStackTrace();
                }
            });
            if (task == null) {
                throw new IllegalStateException("Bungee scheduler rejected Porticus message task");
            }
        } catch (Throwable t) {
            Porticus.INSTANCE.getMissions().remove(getUID(), this);
            throw new IllegalStateException("failed to schedule mission message", t);
        }
    }

    public static void sendBungeeMessage(ProxiedPlayer player, String... args) {
        sendStandalone(resolvePlayer(player), args);
    }

    public static void sendBungeeMessage(Server server, String... args) {
        sendStandalone(resolveServer(server), args);
    }

    public static void sendBungeeMessage(ServerInfo server, String... args) {
        sendStandalone(resolveServerInfo(server), args);
    }

    /**
     * 向指定子服发送消息。
     *
     * @param queue 目标服无活跃连接时是否交由 Bungee 排队。
     *              传 false 时无连接会抛出 {@link IllegalStateException}；
     *              传 true 则静默排队，适用于遍历 {@code getServers()} 向所有子服广播的场景。
     */
    public static void sendBungeeMessage(ServerInfo server, boolean queue, String... args) {
        sendStandalone(resolveServerInfo(server, queue), args);
    }

    private static void sendStandalone(MessageTarget target, String[] args) {
        try {
            Plugin plugin = getPlugin();
            List<byte[]> messages = MessageBuilder.create(args);
            ScheduledTask task = BungeeCord.getInstance().getScheduler().runAsync(plugin, () -> {
                try {
                    sendMessages(target, messages, false);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
            if (task == null) {
                throw new IllegalStateException("Bungee scheduler rejected Porticus message task");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void sendMessages(MessageTarget target, List<byte[]> messages, boolean mission) {
        for (byte[] bytes : messages) {
            if (mission && target instanceof MissionTarget && !((MissionTarget) target).missionPending()) {
                return;
            }
            target.send(bytes);
        }
    }

    private MessageTarget resolveTarget(Object target, boolean tracked) {
        MessageTarget resolved;
        if (target instanceof Server) {
            resolved = resolveServer((Server) target);
        } else if (target instanceof ServerInfo) {
            resolved = resolveServerInfo((ServerInfo) target);
        } else if (target instanceof ProxiedPlayer) {
            resolved = resolvePlayer((ProxiedPlayer) target);
        } else {
            throw new IllegalStateException("target must be Server, ServerInfo or ProxiedPlayer");
        }
        return new MissionTarget(resolved, tracked);
    }

    private static MessageTarget resolvePlayer(ProxiedPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        Server connection = player.getServer();
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("target player is not connected to a server");
        }
        return bytes -> {
            if (player.getServer() != connection || !connection.isConnected()) {
                throw new IllegalStateException("target player changed server before Porticus message was sent");
            }
            connection.sendData(Porticus.INSTANCE.getChannelId(), bytes);
        };
    }

    private static MessageTarget resolveServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        if (server.getInfo() == null || !server.isConnected()) {
            throw new IllegalStateException("target server connection is closed");
        }
        return bytes -> {
            if (!server.isConnected()) {
                throw new IllegalStateException("target server connection is closed");
            }
            server.sendData(Porticus.INSTANCE.getChannelId(), bytes);
        };
    }

    private static MessageTarget resolveServerInfo(ServerInfo server) {
        return resolveServerInfo(server, false);
    }

    /**
     * 解析子服发送目标。
     *
     * @param queue 目标服无活跃连接时是否交由 Bungee 排队。
     *              传 false（默认）会立即抛出 {@link IllegalStateException}，便于调用方感知发送失败；
     *              传 true 则退回旧行为静默排队，适用于「遍历 getServers() 向所有子服广播」这类
     *              不应因个别空服而中断的用法。
     */
    private static MessageTarget resolveServerInfo(ServerInfo server, boolean queue) {
        if (server == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        if (!queue && server.getPlayers().isEmpty()) {
            throw new IllegalStateException("target server has no active player connection");
        }
        return bytes -> {
            if (!server.sendData(Porticus.INSTANCE.getChannelId(), bytes, queue) && !queue) {
                throw new IllegalStateException("target server has no active player connection");
            }
        };
    }

    private static Plugin getPlugin() {
        try {
            Object instance = Class.forName("taboolib.platform.BungeePlugin").getMethod("getInstance").invoke(null);
            if (instance instanceof Plugin) {
                return (Plugin) instance;
            }
        } catch (Throwable t) {
            throw new IllegalStateException("TabooLib BungeePlugin is not available", t);
        }
        throw new IllegalStateException("TabooLib BungeePlugin is not available");
    }

    private interface MessageTarget {

        void send(byte[] bytes);
    }

    private final class MissionTarget implements MessageTarget {

        private final MessageTarget delegate;
        private final boolean tracked;

        private MissionTarget(MessageTarget delegate, boolean tracked) {
            this.delegate = delegate;
            this.tracked = tracked;
        }

        @Override
        public void send(byte[] bytes) {
            delegate.send(bytes);
        }

        private boolean missionPending() {
            return !tracked || MissionBungee.this.isPending();
        }
    }
}
