package me.skymc.taboolib.display;

import java.lang.reflect.Constructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.skymc.taboolib.methods.MethodsUtils;

public class TitleUtils {
    
    private static void sendPacket(Player player, Object packet)
    {
        try
        {
            Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") }).invoke(playerConnection, new Object[] { packet });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private static Class<?> getNMSClass(String class_name)
    {
        String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
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
    
    public static void sendTitle(Player p, String title, int fadeint, int stayt, int fadeoutt, String subtitle, int fadeinst, int stayst, int fadeoutst)
    {
        if (title == null) {
            title = "";
        }
        if (subtitle == null) {
            subtitle = "";
        }
        try
        {
            if (title != null)
            {
                Object e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
                Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + title + "\"}" });
                Constructor<?> subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
                Object titlePacket = subtitleConstructor.newInstance(new Object[] { e, chatTitle, Integer.valueOf(fadeint), Integer.valueOf(stayt), Integer.valueOf(fadeoutt) });
                sendPacket(p, titlePacket);
                
                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
                chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + title + "\"}" });
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent") });
                titlePacket = subtitleConstructor.newInstance(new Object[] { e, chatTitle });
                sendPacket(p, titlePacket);
            }
            if (subtitle != null)
            {
                Object e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
                Object chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + title + "\"}" });
                Constructor<?> subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
                Object subtitlePacket = subtitleConstructor.newInstance(new Object[] { e, chatSubtitle, Integer.valueOf(fadeinst), Integer.valueOf(stayst), Integer.valueOf(fadeoutst) });
                sendPacket(p, subtitlePacket);
                
                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
                chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\":\"" + subtitle + "\"}" });
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
                subtitlePacket = subtitleConstructor.newInstance(new Object[] { e, chatSubtitle, Integer.valueOf(fadeinst), Integer.valueOf(stayst), Integer.valueOf(fadeoutst) });
                sendPacket(p, subtitlePacket);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
