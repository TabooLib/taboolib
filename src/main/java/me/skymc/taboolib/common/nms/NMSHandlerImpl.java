package me.skymc.taboolib.common.nms;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.packet.TPacketHandler;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:42
 */
public class NMSHandlerImpl extends NMSHandler {

    @Override
    public void sendTitle(Player player, String title, int titleFadein, int titleStay, int titleFadeout, String subtitle, int subtitleFadein, int subtitleStay, int subtitleFadeout) {
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(String.valueOf(title))));
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(String.valueOf(subtitle))));
    }

    @Override
    public void sendActionBar(Player player, String text) {
        if (TabooLib.getVersionNumber() > 11100) {
            TPacketHandler.sendPacket(player, new net.minecraft.server.v1_12_R1.PacketPlayOutChat(new net.minecraft.server.v1_12_R1.ChatComponentText(String.valueOf(text)), ChatMessageType.GAME_INFO));
        } else {
            TPacketHandler.sendPacket(player, new PacketPlayOutChat(new ChatComponentText(String.valueOf(text)), (byte) 2));
        }
    }
}
