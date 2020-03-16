package io.izzel.taboolib.module.hologram;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.module.inject.PlayerContainer;
import io.izzel.taboolib.module.inject.TSchedule;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author sky
 * @Since 2020-03-07 14:24
 */
public class THologram {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PlayerContainer
    private static Map<String, Integer> index = Maps.newConcurrentMap();
    private static List<Hologram> holograms = Lists.newCopyOnWriteArrayList();

    public static List<Hologram> getHolograms() {
        return holograms;
    }

    public static int nextIndex(Player player) {
        return index.put(player.getName(), index.computeIfAbsent(player.getName(), e -> 449599702) + 1);
    }

    public static Hologram create(Location location, String text) {
        return new Hologram(location, text);
    }

    public static Hologram create(Location location, String text, Player... viewers) {
        return new Hologram(location, text, viewers);
    }

    @TSchedule(period = 100, async = true)
    public static void release() {
        holograms.removeIf(Hologram::isDeleted);
    }

    public static void remove(Player player) {
        holograms.forEach(hologram -> hologram.removeViewer(player));
    }

    public static void refresh(Player player) {
        for (Hologram hologram : holograms) {
            HologramViewer viewer = hologram.getViewer(player);
            if (viewer != null) {
                hologram.refresh(viewer);
            } else if (hologram.isViewAll()) {
                hologram.addViewer(player);
            }
        }
    }

    public static void submit(Runnable runnable) {
        executorService.submit(() -> {
            if (THologramHandler.isLearned()) {
                runnable.run();
            } else {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
