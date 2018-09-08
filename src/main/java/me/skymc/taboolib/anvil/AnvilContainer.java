package me.skymc.taboolib.anvil;

import org.bukkit.entity.Player;

/**
 * @Author sky
 * @Since 2018-09-08 15:47
 */
public class AnvilContainer extends net.minecraft.server.v1_12_R1.ContainerAnvil {

    public AnvilContainer(net.minecraft.server.v1_12_R1.EntityHuman player) {
        super(player.inventory, player.world, new net.minecraft.server.v1_12_R1.BlockPosition(0, 0, 0), player);
    }

    @Override
    public boolean a(net.minecraft.server.v1_12_R1.EntityHuman player) {
        return true;
    }

    public static void openAnvil(Player p) {
        net.minecraft.server.v1_12_R1.EntityPlayer player = ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) p).getHandle();
        AnvilContainer container = new AnvilContainer(player);
        int c = player.nextContainerCounter();
        player.playerConnection.sendPacket(new net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow(c, "minecraft:anvil", new net.minecraft.server.v1_12_R1.ChatMessage("Repairing"), 0));
        player.activeContainer = container;
        player.activeContainer.windowId = c;
        player.activeContainer.addSlotListener(player);
    }
}