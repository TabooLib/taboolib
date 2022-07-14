package io.izzel.taboolib.module.hologram;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.common.event.PlayerHologramDisplayEvent;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.inject.TSchedule;
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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

/**
 * 全息逻辑处理类
 *
 * @author sky
 * @since 2020-03-07 14:23
 */
@TListener
class THologramHandler implements Listener {

    private static final Queue<THologramSchedule> queueSchedule = Queues.newArrayDeque();
    private static ArmorStand learnTarget = null;
    private static Packet packetInit = null;
    private static Packet packetSpawn = null;
    private static Packet packetName = null;
    private static THologramSchedule currentSchedule = null;
    private static boolean learned = false;

    @TPacket(type = TPacket.Type.RECEIVE)
    static boolean d(Player player, Packet packet) {
        if (packet.is("PacketPlayInPosition") && !learned) {
            learned = true;
            TabooLib.getPlugin().runTask(() -> learn(player));
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
        if ((packet.is("PacketPlayOutSpawnEntityLiving") || packet.is("PacketPlayOutSpawnEntity")) && packet.read("a", 0) == learnTarget.getEntityId()) {
            packetSpawn = packet;
            return false;
        }
        if (packet.is("PacketPlayOutEntityMetadata") && packet.read("a", 0) == learnTarget.getEntityId()) {
            if (currentSchedule != null) {
                currentSchedule.after(packet);
            } else {
                packetInit = packet;
            }
            return false;
        }
        return true;
    }

    /**
     * 是否学习完成。
     * 指 TabooLib 会在首位玩家进入服务器时学习服务端向玩家发送的 ArmorStand 数据包，并在之后借助该数据包结构伪向玩家发送造虚假的 ArmorStand 实体。
     */
    public static boolean isLearned() {
        return packetInit != null && packetSpawn != null && packetName != null;
    }

    /**
     * 克隆一个 ArmorStand 生成数据包
     *
     * @param id       序号
     * @param location 新的实体坐标
     * @return 数据包对象
     */
    public static Packet copy(int id, Location location) throws Exception {
        try {
            Packet packet = THologramHandler.getPacketSpawn().copy("g", "h", "i", "j", "k", "l");
            packet.write("a", id);
            packet.write("b", UUID.randomUUID());
            if (THologramHandler.getPacketSpawn().is("PacketPlayOutSpawnEntityLiving")) {
                packet.write("c", THologramHandler.getPacketSpawn().read("c"));
                packet.write("d", location.getX());
                packet.write("e", location.getY());
                packet.write("f", location.getZ());
                if (Version.isBefore(Version.v1_15)) {
                    packet.write("m", THologramHandler.getPacketSpawn().read("m"));
                    packet.write("n", THologramHandler.getPacketSpawn().read("n"));
                }
            } else if (THologramHandler.getPacketSpawn().is("PacketPlayOutSpawnEntity")) {
                packet.write("c", location.getX());
                packet.write("d", location.getY());
                packet.write("e", location.getZ());
                packet.write("f", THologramHandler.getPacketSpawn().read("f"));
            }
            return packet;
        } catch (Exception e) {
            throw new Exception("THologram 仅支持Minecraft 1.8+");
        }
    }

    public static Packet copy(int id) {
        Packet packet = THologramHandler.getPacketInit().copy("b");
        packet.write("a", id);
        return packet;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Packet copy(HologramViewer viewer, String name) {
        PlayerHologramDisplayEvent event = new PlayerHologramDisplayEvent(viewer.getPlayer(), name).call();
        Packet packet = THologramHandler.getPacketName().copy();
        packet.write("a", viewer.getId());
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
                    SimpleReflection.setFieldValue(element.getClass(), i, "c", c);
                    if (Version.isAfter(Version.v1_14)) {
                        SimpleReflection.setFieldValue(element.getClass(), i, "b", Optional.of(NMS.handle().ofChatComponentText(event.getText())));
                    } else {
                        SimpleReflection.setFieldValue(element.getClass(), i, "b", event.getText());
                    }
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

    /**
     * 重置数据包结构
     */
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

    /**
     * 学习服务端即将向该玩家发送的 ArmorStand 数据包结构。
     *
     * @param player 玩家
     */
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
                    if (currentSchedule != null) {
                        currentSchedule.before();
                    }
                }
            }
        }.runTaskTimer(TabooLib.getPlugin(), 1, 1);
    }

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

    @TSchedule(period = 200, async = true)
    public void e() {
        Bukkit.getOnlinePlayers().forEach(THologram::refresh);
    }


}
