package me.skymc.taboolib.display;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.nms.NMSUtils;

/**
 * @author Bkm016
 * @since 2018-04-26
 */
public class ActionUtils {
	
	private static Class<?> Packet = NMSUtils.getNMSClass("Packet");
	private static Class<?> ChatComponentText = NMSUtils.getNMSClass("ChatComponentText");
	private static Class<?> ChatMessageType = NMSUtils.getNMSClass("ChatMessageType");
	private static Class<?> PacketPlayOutChat = NMSUtils.getNMSClass("PacketPlayOutChat");
	private static Class<?> IChatBaseComponent = NMSUtils.getNMSClass("IChatBaseComponent");
	
    public static void send(Player player, String action) {
        if (player == null) {
            return;
        }
        try {
            Object ab = ChatComponentText.getConstructor(String.class).newInstance(action);
            Constructor<?> ac = null;
            Object abPacket = null;
            if (TabooLib.getVerint() > 11100) {
            	ac = PacketPlayOutChat.getConstructor(IChatBaseComponent, ChatMessageType);
            	abPacket = ac.newInstance(ab, ChatMessageType.getMethod("a", Byte.TYPE).invoke(null, (byte) 2));
            } else {
            	ac = PacketPlayOutChat.getConstructor(IChatBaseComponent, Byte.TYPE);
                abPacket = ac.newInstance(ab, (byte) 2);
            }
            sendPacket(player, abPacket);
        }
        catch (Exception ignored) {
        }
    }
    
    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", Packet).invoke(playerConnection, packet);
        }
        catch (Exception ignored) {
        }
    }
}

