package me.skymc.taboolib.anvil.versions;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer_V1_8_R3 extends ContainerAnvil {

	public AnvilContainer_V1_8_R3(EntityHuman player)
	{
		super(player.inventory, player.world, new BlockPosition(0, 0, 0), player);
	}
	
	public boolean a(EntityHuman player)
	{
		return true;
	}
	  
	/** 
	 * @deprecated 方法已过期，已有新的方法
	 */
	public static void openAnvil(Player p)
	{
		EntityPlayer player = ((CraftPlayer)p).getHandle();
		AnvilContainer_V1_8_R3 container = new AnvilContainer_V1_8_R3(player);
	    int c = player.nextContainerCounter();
        player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(c, "minecraft:anvil", new ChatMessage("Repairing"), 0));
	    player.activeContainer = container;
	    player.activeContainer.windowId = c;
	    player.activeContainer.addSlotListener(player);
	}

}
