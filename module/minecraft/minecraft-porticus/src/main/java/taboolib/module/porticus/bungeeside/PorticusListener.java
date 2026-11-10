package taboolib.module.porticus.bungeeside;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.Message;
import taboolib.module.porticus.common.MessageReader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Bkm016
 * @since 2018-04-16
 */
@SuppressWarnings("DuplicatedCode")
public class PorticusListener implements Listener {

    private final Plugin plugin;
    private final AtomicLong nextCacheWarning = new AtomicLong();

    public PorticusListener() {
        plugin = getPlugin();
        ProxyServer.getInstance().registerChannel(Porticus.INSTANCE.getChannelId());
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
        BungeeCord.getInstance().getScheduler().schedule(plugin, () -> {
            for (PorticusMission mission : Porticus.INSTANCE.getMissions()) {
                if (mission.isTimeout() && Porticus.INSTANCE.getMissions().remove(mission)) {
                    if (mission.getTimeoutRunnable() != null) {
                        try {
                            mission.getTimeoutRunnable().run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
            MessageReader.cleanUp();
        }, 1, 1, TimeUnit.SECONDS);
    }

    @EventHandler
    public void e(PorticusBungeeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        try {
            for (PorticusMission mission : Porticus.INSTANCE.getMissions()) {
                if (mission.getUID().equals(e.getUID()) && Porticus.INSTANCE.getMissions().remove(mission)) {
                    if (mission.getResponseConsumer() != null) {
                        try {
                            mission.getResponseConsumer().accept(e.getArgs());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    return;
                }
            }
            String[] args = e.getArgs();
            if (args.length < 2 || !"porticus".equals(args[0])) {
                return;
            }
            switch (args[1]) {
                case "connect": {
                    if (args.length < 4) {
                        return;
                    }
                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[2]);
                    ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[3]);
                    if (proxiedPlayer != null && serverInfo != null) {
                        proxiedPlayer.connect(serverInfo);
                    }
                    break;
                }
                case "whois": {
                    if (args.length < 3) {
                        return;
                    }
                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[2]);
                    if (proxiedPlayer != null && proxiedPlayer.getServer() != null) {
                        e.response(proxiedPlayer.getServer().getInfo().getName());
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler
    public void e(PluginMessageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getSender() instanceof Server && e.getReceiver() instanceof ProxiedPlayer && e.getTag().equalsIgnoreCase(Porticus.INSTANCE.getChannelId())) {
            try {
                Message message = MessageReader.read(e.getData());
                if (message.isCompleted()) {
                    String[] args = message.buildOnce();
                    if (args != null) {
                        PorticusBungeeEvent.call((Server) e.getSender(), message.getUID(), args);
                    }
                }
            } catch (MessageReader.ProtocolException ignored) {
                // Malformed or oversized plugin messages are rejected without flooding the proxy log.
            } catch (MessageReader.CapacityException ex) {
                warnCacheCapacity(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void warnCacheCapacity(IOException exception) {
        long now = System.currentTimeMillis();
        long next = nextCacheWarning.get();
        if (now >= next && nextCacheWarning.compareAndSet(next, now + 10_000)) {
            plugin.getLogger().warning("Porticus message cache rejected input: " + exception.getMessage());
        }
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
}
