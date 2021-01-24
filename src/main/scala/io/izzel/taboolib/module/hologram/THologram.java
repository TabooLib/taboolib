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
 * 全息工具
 *
 * @author sky
 * @since 2020-03-07 14:24
 */
public class THologram {

    @PlayerContainer
    private static final Map<String, Integer> index = Maps.newConcurrentMap();
    private static final List<Hologram> holograms = Lists.newCopyOnWriteArrayList();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 获取所有已创建的 {@link Hologram} 实例
     *
     * @return 包含所有 {@link Hologram} 实例的列表
     */
    public static List<Hologram> getHolograms() {
        return holograms;
    }

    public static int nextIndex(Player player) {
        int idx = index.computeIfAbsent(player.getName(), e -> 449599702) + 1;
        index.put(player.getName(), idx);
        return idx;
    }

    /**
     * 创建一个 {@link Hologram} 全息字实例
     *
     * @param location 全息字需要被放置到的位置
     * @param text     全系字内容
     * @return {@link Hologram} 全息字实例
     */
    public static Hologram create(Location location, String text) {
        return new Hologram(location, text);
    }

    /**
     * 创建一个 {@link Hologram} 全息字实例
     * * @param location 全息字需要被放置到的位置
     * * @param text     全系字内容
     *
     * @param viewers  能看到该全息字的玩家
     * @param location 坐标
     * @param text     文字
     * @return {@link Hologram} 全息字实例
     */
    public static Hologram create(Location location, String text, Player... viewers) {
        return new Hologram(location, text, viewers);
    }

    /**
     * 释放所有已被声明删除的 {@link Hologram} 全息字对象实例
     */
    @TSchedule(period = 100, async = true)
    public static void release() {
        holograms.removeIf(Hologram::isDeleted);
    }

    /**
     * 移除指定玩家对所有 {@link Hologram} 全息字实例的可见
     *
     * @param player 需要移除的玩家
     */
    public static void remove(Player player) {
        holograms.forEach(hologram -> hologram.removeViewer(player));
    }

    /**
     * 刷新指定玩家对所有 {@link Hologram} 全息字实例的可见
     *
     * @param player 需要刷新的玩家
     */
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
