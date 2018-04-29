package com.ilummc.tlib.nms;

import com.ilummc.tlib.util.asm.AsmClassTransformer;
import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class ActionBar {

    private static ActionBar instance;

    static {
        if (TabooLib.getVerint() > 11100) {
            instance = (ActionBar) AsmClassTransformer.builder().from(Impl_1_12.class).fromVersion("v1_12_R1")
                    .toVersion(Bukkit.getServer().getClass().getName().split("\\.")[3]).build().transform();
        } else {
            instance = (ActionBar) AsmClassTransformer.builder().from(Impl_1_8.class).fromVersion("v1_8_R3")
                    .toVersion(Bukkit.getServer().getClass().getName().split("\\.")[3]).build().transform();
        }
    }

    public static void sendActionBar(Player player, String text) {
        instance.send(player, text);
    }

    public abstract void send(Player player, String text);

    public static class Impl_1_8 extends ActionBar {

        @Override
        public void send(Player player, String text) {
            net.minecraft.server.v1_8_R3.ChatComponentText component = new net.minecraft.server.v1_8_R3.ChatComponentText(text);
            net.minecraft.server.v1_8_R3.PacketPlayOutChat packet = new net.minecraft.server.v1_8_R3.PacketPlayOutChat(component, (byte) 2);
            ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static class Impl_1_12 extends ActionBar {

        @Override
        public void send(Player player, String text) {
            net.minecraft.server.v1_12_R1.ChatComponentText component = new net.minecraft.server.v1_12_R1.ChatComponentText(text);
            net.minecraft.server.v1_12_R1.PacketPlayOutChat packet = new net.minecraft.server.v1_12_R1.PacketPlayOutChat(component,
                    net.minecraft.server.v1_12_R1.ChatMessageType.a((byte) 2));
            ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        }
    }
}
