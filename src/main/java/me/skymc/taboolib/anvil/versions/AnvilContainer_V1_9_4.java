package me.skymc.taboolib.anvil.versions;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer_V1_9_4 extends ContainerAnvil {

    public AnvilContainer_V1_9_4(EntityHuman player) {
        super(player.inventory, player.world, new BlockPosition(0, 0, 0), player);
    }

    @Override
    public boolean a(EntityHuman player) {
        return true;
    }

    /**
     * @deprecated 方法已过期，已有新的方法
     */
    @Deprecated
    public static void openAnvil(Player p) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        AnvilContainer_V1_9_4 container = new AnvilContainer_V1_9_4(player);
        int c = player.nextContainerCounter();
        player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(c, "minecraft:anvil", new ChatMessage("Repairing"), 0));
        player.activeContainer = container;
        player.activeContainer.windowId = c;
        player.activeContainer.addSlotListener(player);
    }

}
