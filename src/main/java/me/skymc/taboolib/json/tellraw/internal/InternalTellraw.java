package me.skymc.taboolib.json.tellraw.internal;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.packet.TPacketHandler;
import me.skymc.taboolib.common.util.SimpleReflection;
import me.skymc.taboolib.json.tellraw.TellrawVersion;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2018-11-07 22:54
 */
public class InternalTellraw implements AbstractTellraw {

    private int bukkitVersion = TabooLib.getVersionNumber();

    public InternalTellraw() {
        SimpleReflection.saveField(NBTTagCompound.class, "map");
        SimpleReflection.saveField(NBTTagList.class, "list");
    }

    @Override
    public void sendRawMessage(Player player, String rawMessage) {
        TPacketHandler.sendPacket(player, new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(rawMessage)));
    }

    @Override
    public String getItemComponent(ItemStack itemStack) {
        return getItemComponent(itemStack, TellrawVersion.CURRENT_VERSION);
    }

    @Override
    public String getItemComponent(ItemStack itemStack, TellrawVersion version) {
        return nbtToString(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()), version);
    }

    private String nbtToString(Object nbtTagCompound, TellrawVersion version) {
        StringBuilder builder = new StringBuilder("{");
        Map map = (Map) SimpleReflection.getFieldValue(NBTTagCompound.class, nbtTagCompound, "map");
        int index = 0;
        for (Object nbtBaseEntry : map.entrySet()) {
            if (index++ != 0) {
                builder.append(",");
            }
            Object value = ((Map.Entry) nbtBaseEntry).getValue();
            if (value instanceof NBTTagList ) {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(nbtListToString(value, version));
            } else if (value instanceof NBTTagCompound) {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(nbtToString(value, version));
            } else {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(value);
            }
        }
        return builder.append('}').toString();
    }

    private String nbtListToString(Object nbtTagList, TellrawVersion version) {
        StringBuilder builder = new StringBuilder("[");
        List list = (List) SimpleReflection.getFieldValue(NBTTagList.class, nbtTagList, "list");
        for (int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                builder.append(',');
            }
            if (version == TellrawVersion.HIGH_VERSION || (this.bukkitVersion >= 11200 && version == TellrawVersion.CURRENT_VERSION)) {
                builder.append(list.get(i));
            } else {
                builder.append(i).append(':').append(list.get(i));
            }
        }
        return builder.append(']').toString();
    }
}
