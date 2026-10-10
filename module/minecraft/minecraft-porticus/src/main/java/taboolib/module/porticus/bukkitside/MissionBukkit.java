package taboolib.module.porticus.bukkitside;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import taboolib.module.porticus.Porticus;
import taboolib.module.porticus.PorticusMission;
import taboolib.module.porticus.common.MessageBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
        if (!(target instanceof Player)) {
            throw new IllegalStateException("target must be Player");
        }
        if (command == null) {
            throw new IllegalStateException("command must be set before running mission");
        }
        List<byte[]> messages;
        try {
            messages = MessageBuilder.create(command);
        } catch (IOException e) {
            throw new IllegalStateException("failed to encode mission command", e);
        }
        boolean tracked = consumer != null || runnable != null;
        super.run(target);
        try {
            scheduleBukkitMessage((Player) target, messages, tracked);
        } catch (Throwable t) {
            Porticus.INSTANCE.getMissions().remove(this);
            throw new IllegalStateException("failed to schedule mission message", t);
        }
    }

    public void sendBukkitMessage(Player player, String[] command) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("player cannot be null");
            }
            scheduleBukkitMessage(player, MessageBuilder.create(command), false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void scheduleBukkitMessage(Player player, List<byte[]> messages, boolean tracked) throws Exception {
        Runnable failure = tracked ? () -> Porticus.INSTANCE.getMissions().remove(this) : () -> {
        };
        Runnable sendTask = () -> {
            if (tracked && !Porticus.INSTANCE.getMissions().contains(this)) {
                return;
            }
            try {
                for (byte[] bytes : messages) {
                    if (tracked && !Porticus.INSTANCE.getMissions().contains(this)) {
                        return;
                    }
                    player.sendPluginMessage(plugin, Porticus.INSTANCE.getChannelId(), bytes);
                }
            } catch (Throwable t) {
                failure.run();
                t.printStackTrace();
            }
        };
        if (isFolia()) {
            runOnEntityScheduler(player, sendTask, failure);
        } else if (Bukkit.isPrimaryThread()) {
            sendTask.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, sendTask);
        }
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, playerClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static ClassLoader playerClassLoader() {
        ClassLoader classLoader = Player.class.getClassLoader();
        return classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    private void runOnEntityScheduler(Player player, Runnable sendTask, Runnable retired) throws Exception {
        Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
        Method runMethod = null;
        for (Method method : scheduler.getClass().getMethods()) {
            if (method.getName().equals("run") && method.getParameterTypes().length == 3) {
                runMethod = method;
                break;
            }
        }
        if (runMethod == null) {
            throw new NoSuchMethodException("EntityScheduler#run");
        }
        Consumer<Object> task = ignored -> sendTask.run();
        Object scheduled = runMethod.invoke(scheduler, plugin, task, retired);
        if (scheduled == null) {
            throw new IllegalStateException("EntityScheduler rejected Porticus message task");
        }
    }
}
