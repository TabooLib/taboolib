package io.izzel.taboolib.util.lite;

import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.impl.Position;
import io.izzel.taboolib.module.packet.Packet;
import io.izzel.taboolib.module.packet.TPacket;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

/**
 * @Author sky
 * @Since 2020-01-14 21:43
 */
public class Signs {

    private static final List<Data> signs = Lists.newCopyOnWriteArrayList();

    /**
     * 向玩家发送虚拟牌子，并返回编辑内容
     *
     * @param player  玩家
     * @param origin  原始内容
     * @param catcher 编辑内容
     */
    public static void fakeSign(Player player, String[] origin, Consumer<String[]> catcher) {
        Validate.isTrue(Version.isAfter(Version.v1_8), "Unsupported Version: " + Version.getCurrentVersion());
        Location location = player.getLocation();
        location.setY(0);
        try {
            player.sendBlockChange(location, Materials.OAK_WALL_SIGN.parseMaterial(), (byte) 0);
            player.sendSignChange(location, format(origin));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        NMS.handle().openSignEditor(player, location.getBlock());
        signs.add(new Data(player.getName(), catcher, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public static void fakeSign(Player player, Consumer<String[]> catcher) {
        fakeSign(player, new String[0], catcher);
    }

    public static boolean isSign(Block block) {
        return block.getState() instanceof Sign;
    }

    public static String[] getLines(Block block) {
        return isSign(block) ? ((Sign) block.getState()).getLines() : null;
    }

    public static void setLines(Block block, String[] lines) {
        if (isSign(block)) {
            Sign sign = (Sign) block.getState();
            for (int i = 0; i < lines.length && i < 4; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update();
        }
    }

    @TPacket(type = TPacket.Type.RECEIVE)
    static boolean onPacket(Player player, Packet packet) {
        if (packet.is("PacketPlayInUpdateSign")) {
            try {
                Position position = NMS.handle().fromBlockPosition(packet.read("a"));
                if (position == null) {
                    return true;
                }
                Data data = getData(player, position);
                if (data == null) {
                    return true;
                }
                TabooLib.getPlugin().runTask(() -> {
                    data.catcher.accept(packet.read("b", new String[0]));
                });
                signs.remove(data);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }
        return true;
    }

    static String[] format(String[] signs) {
        List<String> list = Lists.newArrayList(signs);
        while (list.size() < 4) {
            list.add("");
        }
        while (list.size() > 4) {
            list.remove(4);
        }
        return list.toArray(new String[0]);
    }

    static Data getData(Player player, Position position) {
        return signs.stream().filter(sign -> sign.player.equals(player.getName()) && sign.isSign(position)).findFirst().orElse(null);
    }

    static class Data {

        private final String player;
        private final Consumer<String[]> catcher;
        private final int x;
        private final int y;
        private final int z;

        public Data(String player, Consumer<String[]> catcher, int x, int y, int z) {
            this.player = player;
            this.catcher = catcher;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean isSign(Position position) {
            return this.x == position.getX() && this.y == position.getY() && this.z == position.getZ();
        }
    }
}
