package io.izzel.taboolib.module.hologram;

import com.google.common.collect.Sets;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.TPacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 全息实例
 *
 * @Author sky
 * @Since 2020-03-07 16:28
 */
public class Hologram {

    private final Set<HologramViewer> viewers = Sets.newConcurrentHashSet();
    private String text;
    private Location location;
    private boolean viewAll = false;
    private boolean deleted = false;
    private boolean autoDelete = false;
    private int viewDistance = 50;
    private Consumer<Player> event;

    /**
     * @see THologram#create(Location, String, Player...)
     */
    Hologram(Location location, String text, Player... viewers) {
        THologram.getHolograms().add(this);
        this.text = text;
        this.location = location.clone();
        for (Player viewer : viewers) {
            addViewer(viewer);
        }
    }

    /**
     * 设置当该全息实例在没有任何观察者 {@link HologramViewer} 时自动删除。
     */
    public Hologram autoDelete() {
        this.autoDelete = true;
        return this;
    }

    /**
     * 设置全息字对所有玩家显示
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram toAll() {
        this.viewAll = true;
        return refresh();
    }

    /**
     * 设置全息字对所有玩家隐藏
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram hideAll() {
        this.viewAll = false;
        return removeViewers();
    }

    /**
     * 设置全息字对是否对所有玩家显示
     * 设置为 false 则该全息字实例将对所有玩家隐藏
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram viewAll(Boolean viewAll) {
        if (!this.viewAll && viewAll) {
            toAll();
        }
        if (this.viewAll && !viewAll) {
            hideAll();
        }
        return this;
    }

//    public Hologram onClick(Consumer<Player> event) {
//        this.event = event;
//        return this;
//    }

    /**
     * 刷新指定玩家对该全息字实例的可见
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram refresh() {
        if (deleted) {
            return this;
        }
        if (viewAll) {
            Bukkit.getOnlinePlayers().forEach(this::addViewer);
        }
        viewers.forEach(this::refresh);
        return this;
    }

    /**
     * 刷新指定 {@link HologramViewer} 对该全息字实例的可见
     *
     * @param viewer 需要刷新全息字的 {@link HologramViewer}
     * @return 修改后的 Hologram 实例
     */
    public Hologram refresh(HologramViewer viewer) {
        if (deleted) {
            return this;
        }
        viewer.setVisible(location.getWorld().equals(viewer.getPlayer().getWorld()) && location.distance(viewer.getPlayer().getLocation()) < viewDistance);
        if (viewer.isVisible()) {
            THologram.submit(() -> {
                try {
                    if (viewer.isSpawned()) {
                        NMS.handle().sendPacketEntityTeleport(viewer.getPlayer(), viewer.getId(), location);
                    } else {
                        viewer.setSpawned(true);
                        TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId(), location).get());
                        TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId()).get());
                    }
                    TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId(), text).get());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            destroy(viewer);
        }
        return this;
    }

    /**
     * 设置全息字闪烁
     *
     * @param text   需要闪烁的字符串列表
     * @param period 闪烁间隔
     * @return 修改后的 Hologram 实例
     */
    public Hologram flash(List<String> text, int period) {
        for (int i = 0; i < text.size(); i++) {
            String line = text.get(i);
            TabooLib.getPlugin().runTask(() -> flash(line), period * i);
        }
        return this;
    }

    /**
     * 设置全息字闪烁
     *
     * @param text 需要闪烁的字符串
     * @return 修改后的 Hologram 实例
     */
    public Hologram flash(String text) {
        if (deleted) {
            return this;
        }
        this.text = text;
        THologram.submit(() -> {
            try {
                viewers.forEach(v -> TPacketHandler.sendPacket(v.getPlayer(), THologramHandler.copy(v.getId(), text).get()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return this;
    }

    /**
     * 使该全息字以特定间隔不断向某个相对坐标位移
     *
     * @param vector 相对坐标
     * @param period 位移周期
     * @param times  位移次数
     */
    public Hologram flash(Vector vector, int period, int times) {
        for (int i = 0; i < times; i++) {
            TabooLib.getPlugin().runTask(() -> flash(location.add(vector)), period * i);
        }
        return this;
    }

    /**
     * 使该全息字传送至某个坐标
     *
     * @param location 坐标
     */
    public Hologram flash(Location location) {
        if (deleted) {
            return this;
        }
        this.location = location.clone();
        THologram.submit(() -> {
            try {
                viewers.forEach(v -> NMS.handle().sendPacketEntityTeleport(v.getPlayer(), v.getId(), location));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return this;
    }

    /**
     * 声明删除本实例
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram delete() {
        destroy();
        deleted = true;
        return this;
    }

    /**
     * 声明在指定时长后删除本实例
     *
     * @param delay 删除时长
     * @return 修改后的 Hologram 实例
     */
    public Hologram deleteOn(int delay) {
        TabooLib.getPlugin().runTask(this::delete, delay);
        return this;
    }

    /**
     * 声明破坏本实例
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram destroy() {
        if (deleted) {
            return this;
        }
        viewers.forEach(this::destroy);
        return this;
    }

    /**
     * 声明向指定 {@link HologramViewer} 破坏本实例
     *
     * @param viewer 需要声明破坏的 {@link HologramViewer}
     * @return 修改后的 Hologram 实例
     */
    public Hologram destroy(HologramViewer viewer) {
        if (deleted) {
            return this;
        }
        viewer.setSpawned(false);
        THologram.submit(() -> {
            try {
                NMS.handle().sendPacketEntityDestroy(viewer.getPlayer(), viewer.getId());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        return this;
    }

    /**
     * 为本全息字实例添加可见玩家
     *
     * @param player 需要添加可见的玩家
     * @return 修改后的 Hologram 实例
     */
    public Hologram addViewer(Player player) {
        if (deleted) {
            return this;
        }
        if (!isViewer(player)) {
            HologramViewer viewer = new HologramViewer(player);
            viewers.add(viewer);
            refresh(viewer);
        }
        return this;
    }

    /**
     * 为本全息字实例移除可见玩家
     *
     * @param player 需要移除可见的玩家
     * @return 修改后的 Hologram 实例
     */
    public Hologram removeViewer(Player player) {
        if (deleted) {
            return this;
        }
        HologramViewer viewer = getViewer(player);
        if (viewer == null) {
            return this;
        }
        viewers.remove(viewer);
        destroy(viewer);
        if (viewers.isEmpty()) {
            deleted = autoDelete;
        }
        return this;
    }

    /**
     * 移除所有玩家对本全息字的可见
     *
     * @return 修改后的 Hologram 实例
     */
    public Hologram removeViewers() {
        if (deleted) {
            return this;
        }
        destroy();
        viewers.clear();
        deleted = autoDelete;
        return this;
    }

    /**
     * 查询指定玩家是否为本全息字的可见者
     *
     * @param player 需要查询的玩家
     * @return 修改后的 Hologram 实例
     */
    public boolean isViewer(Player player) {
        return viewers.stream().anyMatch(i -> i.getPlayer().getName().equals(player.getName()));
    }

    /**
     * 获取指定玩家的 {@link HologramViewer} 对象实例
     *
     * @param player 玩家
     * @return {@link HologramViewer} 对象实例
     */
    public HologramViewer getViewer(Player player) {
        return viewers.stream().filter(i -> i.getPlayer().getName().equals(player.getName())).findFirst().orElse(null);
    }

    public Set<HologramViewer> getViewers() {
        return viewers;
    }

    public String getText() {
        return text;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public Consumer<Player> getEvent() {
        return event;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }
}
