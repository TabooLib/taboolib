package io.izzel.taboolib.module.hologram;

import com.google.common.collect.Sets;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.TPacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @Author sky
 * @Since 2020-03-07 16:28
 */
public class Hologram {

    private Set<HologramViewer> viewers = Sets.newConcurrentHashSet();
    private String text;
    private Location location;
    private boolean viewAll = false;
    private boolean deleted = false;
    private boolean autoDelete = false;
    private int viewDistance = 50;
    private Consumer<Player> event;

    Hologram(Location location, String text, Player... viewers) {
        THologram.getHolograms().add(this);
        this.text = text;
        this.location = location.clone();
        for (Player viewer : viewers) {
            addViewer(viewer);
        }
    }

    public Hologram toAll() {
        this.viewAll = true;
        return this;
    }

    public Hologram autoDelete() {
        this.autoDelete = true;
        return this;
    }

//    public Hologram onClick(Consumer<Player> event) {
//        this.event = event;
//        return this;
//    }

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

    public Hologram refresh(HologramViewer viewer) {
        if (deleted) {
            return this;
        }
        viewer.setVisible(location.getWorld().equals(viewer.getPlayer().getWorld()) && location.distance(viewer.getPlayer().getLocation()) < viewDistance);
        if (viewer.isVisible()) {
            THologram.submit(() -> {
                if (viewer.isSpawned()) {
                    NMS.handle().sendPacketEntityTeleport(viewer.getPlayer(), viewer.getId(), location);
                } else {
                    viewer.setSpawned(true);
                    TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId(), location).get());
                    TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId()).get());
                }
                TPacketHandler.sendPacket(viewer.getPlayer(), THologramHandler.copy(viewer.getId(), text).get());
            });
        } else {
            destroy(viewer);
        }
        return this;
    }

    public Hologram flash(List<String> text, int period) {
        for (int i = 0; i < text.size(); i++) {
            String line = text.get(i);
            Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), () -> flash(line), period * i);
        }
        return this;
    }

    public Hologram flash(String text) {
        if (deleted) {
            return this;
        }
        this.text = text;
        THologram.submit(() -> viewers.forEach(v -> TPacketHandler.sendPacket(v.getPlayer(), THologramHandler.copy(v.getId(), text).get())));
        return this;
    }

    public Hologram flash(Location location) {
        if (deleted) {
            return this;
        }
        this.location = location.clone();
        THologram.submit(() -> viewers.forEach(v -> NMS.handle().sendPacketEntityTeleport(v.getPlayer(), v.getId(), location)));
        return this;
    }

    public Hologram delete() {
        destroy();
        deleted = true;
        return this;
    }

    public Hologram deleteOn(int delay) {
        Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), this::delete, delay);
        return this;
    }

    public Hologram destroy() {
        if (deleted) {
            return this;
        }
        viewers.forEach(this::destroy);
        return this;
    }

    public Hologram destroy(HologramViewer viewer) {
        if (deleted) {
            return this;
        }
        viewer.setSpawned(false);
        THologram.submit(() -> NMS.handle().sendPacketEntityDestroy(viewer.getPlayer(), viewer.getId()));
        return this;
    }

    public void addViewer(Player player) {
        if (deleted) {
            return;
        }
        if (!isViewer(player)) {
            HologramViewer viewer = new HologramViewer(player);
            viewers.add(viewer);
            refresh(viewer);
        }
    }

    public void removeViewer(Player player) {
        if (deleted) {
            return;
        }
        HologramViewer viewer = getViewer(player);
        viewers.remove(viewer);
        destroy(viewer);
        if (viewers.isEmpty() && autoDelete) {
            deleted = true;
        }
    }

    public boolean isViewer(Player player) {
        return viewers.stream().anyMatch(i -> i.getPlayer().getName().equals(player.getName()));
    }

    public HologramViewer getViewer(Player player) {
        return viewers.stream().filter(i -> i.getPlayer().getName().equals(player.getName())).findFirst().orElse(null);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

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
