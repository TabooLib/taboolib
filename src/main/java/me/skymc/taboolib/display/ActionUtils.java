package me.skymc.taboolib.display;

import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionUtils {
	
    private static void sendPacket(Player player, Object packet)
    {
        try
        {
            Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", new Class[]{getNMSClass("Packet")}).invoke(playerConnection, packet);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static Class<?> getNMSClass(String class_name)
    {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try
        {
            return Class.forName("net.minecraft.server." + version + "." + class_name);
        }
        catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static void send(Player p, String msg)
    {
        if (msg == null) {
            msg = "";
        }
        try
        {
            Object ab = getNMSClass("ChatComponentText").getConstructor(new Class[]{String.class}).newInstance(msg);
            Constructor<?> ac = null;
            Object abPacket = null;
            // 如果版本大于 1.11.0
            if (TabooLib.getVerint() > 11100) {
            	Class<?> chatMessageType = getNMSClass("ChatMessageType");
            	ac = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), chatMessageType);
            	abPacket = ac.newInstance(ab, chatMessageType.getMethod("a", Byte.TYPE).invoke(null, (byte) 2));
            } else {
            	ac = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), Byte.TYPE);
                abPacket = ac.newInstance(ab, (byte) 2);
            }
            sendPacket(p, abPacket);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}

