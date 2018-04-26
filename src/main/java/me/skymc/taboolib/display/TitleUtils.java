package me.skymc.taboolib.display;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import me.skymc.taboolib.nms.NMSUtils;

/**
 * @author Bkm016
 * @since 2018-04-26
 */
public class TitleUtils {
	
	private static Class<?> Packet = NMSUtils.getNMSClass("Packet");
	private static Class<?> PacketPlayOutTitle = NMSUtils.getNMSClass("PacketPlayOutTitle");
	private static Class<?> IChatBaseComponent = NMSUtils.getNMSClass("IChatBaseComponent");
	private static Class<?> EnumTitleAction = PacketPlayOutTitle.getDeclaredClasses()[0];
    
    public static void sendTitle(Player p, String title, String subtitle, int fadein, int stay, int fadeout) {
    	sendTitle(p, title, fadein, stay, fadeout, subtitle, fadein, stay, fadeout);
    }
    
    public static void sendTitle(Player p, String title, int fadeint, int stayt, int fadeoutt, String subtitle, int fadeinst, int stayst, int fadeoutst) {
    	if (p == null) {
    		return;
    	}
    	try {
            if (title != null) {
                Object times = EnumTitleAction.getField("TIMES").get(null);
                Object chatTitle = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                Constructor<?> subtitleConstructor = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                Object titlePacket = subtitleConstructor.newInstance(times, chatTitle, fadeint, stayt, fadeoutt);
                sendPacket(p, titlePacket);
                
                times = EnumTitleAction.getField("TITLE").get(null);
                chatTitle = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                subtitleConstructor = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent);
                titlePacket = subtitleConstructor.newInstance(times, chatTitle);
                sendPacket(p, titlePacket);
            }
            if (subtitle != null) {
                Object times = EnumTitleAction.getField("TIMES").get(null);
                Object chatSubtitle = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                Constructor<?> subtitleConstructor = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                Object subtitlePacket = subtitleConstructor.newInstance(times, chatSubtitle, fadeinst, stayst, fadeoutst);
                sendPacket(p, subtitlePacket);
                
                times = EnumTitleAction.getField("SUBTITLE").get(null);
                chatSubtitle = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
                subtitleConstructor = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                subtitlePacket = subtitleConstructor.newInstance(times, chatSubtitle, fadeinst, stayst, fadeoutst);
                sendPacket(p, subtitlePacket);
            }
        }
        catch (Exception ignored) {
        }
    }
    
    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getDeclaredMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getDeclaredField("playerConnection").get(handle);
            playerConnection.getClass().getDeclaredMethod("sendPacket", Packet).invoke(playerConnection, packet);
        } catch (Exception ignored) {
        }
    }
}
