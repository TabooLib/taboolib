package io.izzel.taboolib.module.hologram;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacket;
import io.izzel.taboolib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * @Author sky
 * @Since 2020-03-07 14:23
 */
@TListener
class THologramHandler implements Listener {

    private static ArmorStand learnTarget = null;
    private static Packet packetInit = null;
    private static Packet packetSpawn = null;
    private static Packet packetName = null;
    private static THologramSchedule currentSchedule = null;
    private static Queue<THologramSchedule> queueSchedule = Queues.newArrayDeque();

    private static boolean learned = false;

    @TPacket(type = TPacket.Type.RECEIVE)
    static boolean d(Player player, Packet packet) {
        if (packet.is("PacketPlayInPosition") && !learned) {
            learned = true;
            Bukkit.getScheduler().runTask(TabooLib.getPlugin(), () -> learn(player));
        }
        if (packet.is("PacketPlayInUseEntity")) {
            int id = packet.read("a", Integer.TYPE);
            for (Hologram hologram : THologram.getHolograms()) {
                HologramViewer viewer = hologram.getViewer(player);
                if (viewer != null && viewer.getId() == id) {
                    hologram.getEvent().accept(player);
                }
            }
        }
        return true;
    }

    @TPacket(type = TPacket.Type.SEND)
    static boolean e(Player player, Packet packet) {
        if (learnTarget == null) {
            return true;
        }
        if (packet.is("PacketPlayOutSpawnEntity") && packet.read("a", Integer.TYPE) == learnTarget.getEntityId()) {
            packetSpawn = packet;
            return false;
        }
        if (packet.is("PacketPlayOutEntityMetadata") && packet.read("a", Integer.TYPE) == learnTarget.getEntityId()) {
            if (currentSchedule != null) {
                currentSchedule.after(packet);
            } else {
                packetInit = packet;
            }
            return false;
        }
        return true;
    }

    public static boolean isLearned() {
        return packetInit != null && packetSpawn != null && packetName != null;
    }

    public static Packet copy(int id, Location location) {
        Packet packet = THologramHandler.getPacketSpawn().copy("e", "f", "j", "h", "i", "j", "k", "l");
        packet.write("a", id);
        if (Version.isAfter(Version.v1_9)) {
            packet.write("b", UUID.randomUUID());
            packet.write("c", location.getX());
            packet.write("d", location.getY());
            packet.write("e", location.getZ());
        } else {
            packet.write("b", location.getX());
            packet.write("c", location.getY());
            packet.write("d", location.getZ());
        }
        return packet;
    }

    public static Packet copy(int id) {
        Packet packet = THologramHandler.getPacketInit().copy("b");
        packet.write("a", id);
        return packet;
    }

    public static Packet copy(int id, String name) {
        Packet packet = THologramHandler.getPacketName().copy();
        packet.write("a", id);
        List copy = Lists.newArrayList();
        List item = THologramHandler.getPacketName().read("b", List.class);
        for (Object element : item) {
            SimpleReflection.checkAndSave(element.getClass());
            if (Version.isAfter(Version.v1_9)) {
                Object a = SimpleReflection.getFieldValue(element.getClass(), element, "a");
                Object c = SimpleReflection.getFieldValue(element.getClass(), element, "c");
                try {
                    Object i = Ref.getUnsafe().allocateInstance(element.getClass());
                    SimpleReflection.setFieldValue(element.getClass(), i, "a", a);
                    SimpleReflection.setFieldValue(element.getClass(), i, "b", name);
                    SimpleReflection.setFieldValue(element.getClass(), i, "c", c);
                    copy.add(i);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            } else {
                Object a = SimpleReflection.getFieldValue(element.getClass(), element, "a");
                Object b = SimpleReflection.getFieldValue(element.getClass(), element, "b");
                Object d = SimpleReflection.getFieldValue(element.getClass(), element, "d");
                try {
                    Object i = Ref.getUnsafe().allocateInstance(element.getClass());
                    SimpleReflection.setFieldValue(element.getClass(), i, "a", a);
                    SimpleReflection.setFieldValue(element.getClass(), i, "b", b);
                    SimpleReflection.setFieldValue(element.getClass(), i, "c", name);
                    SimpleReflection.setFieldValue(element.getClass(), i, "d", d);
                    copy.add(i);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        packet.write("b", copy);
        return packet;
    }

    public static void reset() {
        queueSchedule.clear();
        queueSchedule.offer(new THologramSchedule(() -> packetInit != null) {
            @Override
            public void before() {
                learnTarget.setCustomName("  ");
            }

            @Override
            public void after(Packet packet) {
                packetName = packet;
            }
        });
    }

    public static void learn(Player player) {
        NMS.handle().spawn(player.getLocation(), ArmorStand.class, c -> {
            learnTarget = c;
            learnTarget.setMarker(true);
            learnTarget.setVisible(false);
            learnTarget.setCustomName(" ");
            learnTarget.setCustomNameVisible(true);
            learnTarget.setBasePlate(false);
        });
        reset();
        new BukkitRunnable() {
            @Override
            public void run() {
                THologramSchedule schedule = queueSchedule.peek();
                if (schedule == null) {
                    cancel();
                    learnTarget.remove();
                } else if (schedule.check()) {
                    currentSchedule = queueSchedule.poll();
                    currentSchedule.before();
                }
            }
        }.runTaskTimer(TabooLib.getPlugin(), 1, 1);
    }

    @EventHandler
    public void e(PlayerJoinEvent e) {
        THologram.refresh(e.getPlayer());
    }

    @EventHandler
    public void e(PlayerQuitEvent e) {
        THologram.remove(e.getPlayer());
    }

    @EventHandler
    public void e(PlayerTeleportEvent e) {
        THologram.refresh(e.getPlayer());
    }

    @EventHandler
    public void e(PlayerChangedWorldEvent e) {
        THologram.refresh(e.getPlayer());
    }

    @EventHandler
    public void e(PlayerMoveEvent e) {
        if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
            Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> THologram.refresh(e.getPlayer()));
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static ArmorStand getLearnTarget() {
        return learnTarget;
    }

    public static Packet getPacketSpawn() {
        return packetSpawn;
    }

    public static Packet getPacketInit() {
        return packetInit;
    }

    public static Packet getPacketName() {
        return packetName;
    }
}
