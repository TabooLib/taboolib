package me.skymc.taboolib.anvil;

import me.skymc.taboolib.nms.NMSUtils;
import org.bukkit.entity.Player;

/**
 * @author sky
 */
public class AnvilContainer {

//    private static IAnvilContainer instance;

    private static Class<?> ChatMessage = NMSUtils.getNMSClass("ChatMessage");
    private static Class<?> PacketPlayOutOpenWindow = NMSUtils.getNMSClass("PacketPlayOutOpenWindow");
    private static Class<?> IChatBaseComponent = NMSUtils.getNMSClass("IChatBaseComponent");
    private static Class<?> Packet = NMSUtils.getNMSClass("Packet");

//    public static IAnvilContainer getInstance() {
//        return instance;
//    }
//
//    static {
//        /*
//         * 玩不懂玩不懂... 似乎不会更改父类的包名?
//         */
//        instance = (IAnvilContainer) AsmClassTransformer.builder()
//                .from(AnvilContainerImpl.class)
//                .fromVersion("v1_12_R1")
//                .toVersion(Bukkit.getServer().getClass().getName().split("\\.")[3])
//                .build()
//                .transform();
//    }

    public static void openAnvil(Player p) {
        try {
            Object player = p.getClass().getMethod("getHandle").invoke(p);
            int c = (int) player.getClass().getMethod("nextContainerCounter").invoke(player);
            Object chatMessage = ChatMessage.getConstructor(String.class, Object[].class).newInstance("Repairing", new Object[0]);
            Object packetPlayOutOpenWindow = PacketPlayOutOpenWindow.getConstructor(Integer.TYPE, String.class, IChatBaseComponent, Integer.TYPE).newInstance(c, "minecraft:anvil", chatMessage, 0);
            Object playerConnection = player.getClass().getDeclaredField("playerConnection").get(player);
            playerConnection.getClass().getMethod("sendPacket", Packet).invoke(playerConnection, packetPlayOutOpenWindow);
        } catch (Exception ignored) {
        }
    }
}

//interface IAnvilContainer {
//
//    /**
//     * 打开铁砧界面
//     *
//     * @param player 玩家
//     */
//    void openAnvil(Player player);
//}
//
//class AnvilContainerImpl extends net.minecraft.server.v1_12_R1.ContainerAnvil implements IAnvilContainer {
//
//    public AnvilContainerImpl(net.minecraft.server.v1_12_R1.EntityHuman player) {
//        super(player.inventory, player.world, new net.minecraft.server.v1_12_R1.BlockPosition(0, 0, 0), player);
//    }
//
//    @Override
//    public void openAnvil(Player p) {
//        net.minecraft.server.v1_12_R1.EntityPlayer player = ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) p).getHandle();
//        AnvilContainerImpl container = new AnvilContainerImpl(player);
//        int c = player.nextContainerCounter();
//        player.playerConnection.sendPacket(new net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow(c, "minecraft:anvil", new net.minecraft.server.v1_12_R1.ChatMessage("Repairing"), 0));
//        player.activeContainer = container;
//        player.activeContainer.windowId = c;
//        player.activeContainer.addSlotListener(player);
//    }
//
//    @Override
//    public boolean a(net.minecraft.server.v1_12_R1.EntityHuman player) {
//        return true;
//    }
//}
