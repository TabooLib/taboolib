package me.skymc.taboolib.common.nms;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.packet.TPacketHandler;
import me.skymc.taboolib.nms.NMSUtils;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.Field;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:42
 */
public class NMSHandlerImpl extends NMSHandler {

    private Field entityTypesField;

    public NMSHandlerImpl() {
        if (TabooLib.getVersionNumber() >= 11300) {
            for (Field declaredField : NMSUtils.getNMSClass("Entity").getDeclaredFields()) {
                if (declaredField.getType().getSimpleName().equals("EntityTypes")) {
                    declaredField.setAccessible(true);
                    entityTypesField = declaredField;
                    break;
                }
            }
        }
    }

    @Override
    public String getName(ItemStack itemStack) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (TabooLib.getVersionNumber() >= 11300) {
            String name = ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getItem().getName();
            if (itemStack.getItemMeta() instanceof PotionMeta) {
                name += ".effect." + ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag().getString("Potion").replaceAll("minecraft:(strong_|long_)?", "");
            }
            return name;
        } else {
            String name = ((net.minecraft.server.v1_12_R1.ItemStack) nmsItem).getItem().a((net.minecraft.server.v1_12_R1.ItemStack) nmsItem);
            if (itemStack.getItemMeta() instanceof PotionMeta) {
                return name.replace("item.", "") + ".effect." + ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag().getString("Potion").replaceAll("minecraft:(strong_|long_)?", "");
            }
            return name + ".name";
        }
    }

    @Override
    public String getName(Entity entity) {
        if (TabooLib.getVersionNumber() < 11300) {
            try {
                if (entity instanceof Player) {
                    return "entity.Player.name";
                }
                if (entity instanceof Villager) {
                    String name = "name";
                    Object villager = ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager) entity).getHandle();
                    Object export = new NBTTagCompound();
                    ((EntityVillager) villager).b((NBTTagCompound) export);
                    int career = ((NBTTagCompound) export).getInt("Career");
                    switch (((EntityVillager) villager).getProfession()) {
                        case 0:
                            if (career == 1) {
                                name = "farmer";
                            } else if (career == 2) {
                                name = "fisherman";
                            } else if (career == 3) {
                                name = "shepherd";
                            } else if (career == 4) {
                                name = "fletcher";
                            }
                            break;
                        case 1:
                            if (career == 1) {
                                name = "librarian";
                            } else if (career == 2) {
                                name = "cartographer";
                            }
                            break;
                        case 2:
                            name = "cleric";
                            break;
                        case 3:
                            if (career == 1) {
                                name = "armor";
                            } else if (career == 2) {
                                name = "weapon";
                            } else if (career == 3) {
                                name = "tool";
                            }
                            break;
                        case 4:
                            if (career == 1) {
                                name = "butcher";
                            } else if (career == 2) {
                                name = "leather";
                            }
                            break;
                        case 5:
                            name = "nitwit";
                            break;
                    }
                    return "entity.Villager." + name;
                }
                return "entity." + entity.getType().getEntityClass().getSimpleName() + ".name";
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return "entity.null";
        } else {
            try {
                String name = "entity.minecraft." + IRegistry.ENTITY_TYPE.getKey((net.minecraft.server.v1_13_R2.EntityTypes<?>) entityTypesField.get(((org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity) entity).getHandle())).getKey();
                if (entity instanceof Villager && ((CraftVillager) entity).getCareer() != null) {
                    name += "." + String.valueOf(((CraftVillager) entity).getCareer()).toLowerCase();
                }
                return name;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return "entity.null";
        }
    }

    @Override
    public void sendTitle(Player player, String title, int titleFadein, int titleStay, int titleFadeout, String subtitle, int subtitleFadein, int subtitleStay, int subtitleFadeout) {
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, new ChatComponentText(String.valueOf(title)), titleFadein, titleStay, titleFadeout));
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(String.valueOf(title)), titleFadein, titleStay, titleFadeout));
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, new ChatComponentText(String.valueOf(subtitle)), subtitleFadein, subtitleStay, subtitleFadeout));
        TPacketHandler.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(String.valueOf(subtitle)), subtitleFadein, subtitleStay, subtitleFadeout));
    }

    @Override
    public void sendActionBar(Player player, String text) {
        if (TabooLib.getVersionNumber() > 11100) {
            TPacketHandler.sendPacket(player, new net.minecraft.server.v1_12_R1.PacketPlayOutChat(new net.minecraft.server.v1_12_R1.ChatComponentText(String.valueOf(text)), ChatMessageType.GAME_INFO));
        } else {
            TPacketHandler.sendPacket(player, new PacketPlayOutChat(new ChatComponentText(String.valueOf(text)), (byte) 2));
        }
    }

    @Override
    public double[] getTPS() {
        return MinecraftServer.getServer().recentTps;
    }
}
