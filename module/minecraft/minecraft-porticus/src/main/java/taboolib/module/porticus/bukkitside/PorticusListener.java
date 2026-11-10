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
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.Message;
import taboolib.module.porticus.common.MessageReader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author 坏黑
 * @since 2020-10-15
 */
@SuppressWarnings("DuplicatedCode")
public class PorticusListener implements Listener, PluginMessageListener {

    private final Plugin plugin;
    private final AtomicLong nextCacheWarning = new AtomicLong();

    public PorticusListener() {
        plugin = JavaPlugin.getProvidingPlugin(Porticus.class);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, Porticus.INSTANCE.getChannelId(), this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, Porticus.INSTANCE.getChannelId());
        Runnable timeoutTask = () -> {
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
        };
        if (isFolia()) {
            runGlobalTimer(plugin, timeoutTask);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, timeoutTask, 0, 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PorticusBukkitEvent e) {
        for (PorticusMission mission : Porticus.INSTANCE.getMissions()) {
            if (mission.getUID().equals(e.getUID()) && Porticus.INSTANCE.getMissions().remove(mission)) {
                if (mission.getResponseConsumer() != null) {
                    try {
                        mission.getResponseConsumer().accept(e.getArgs());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (channel.equalsIgnoreCase(Porticus.INSTANCE.getChannelId())) {
            try {
                Message message = MessageReader.read(bytes);
                if (message.isCompleted()) {
                    String[] args = message.buildOnce();
                    if (args != null) {
                        PorticusBukkitEvent.call(player, message.getUID(), args);
                    }
                }
            } catch (MessageReader.ProtocolException ignored) {
                // Malformed or oversized plugin messages are rejected without flooding the server log.
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

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void runGlobalTimer(Plugin plugin, Runnable runnable) {
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method runAtFixedRate = null;
            for (Method method : scheduler.getClass().getMethods()) {
                if (method.getName().equals("runAtFixedRate") && method.getParameterTypes().length == 4) {
                    runAtFixedRate = method;
                    break;
                }
            }
            if (runAtFixedRate == null) {
                throw new NoSuchMethodException("GlobalRegionScheduler#runAtFixedRate");
            }
            Consumer<Object> task = ignored -> runnable.run();
            runAtFixedRate.invoke(scheduler, plugin, task, 1L, 20L);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to schedule Porticus timeout task on Folia", t);
        }
    }
}
