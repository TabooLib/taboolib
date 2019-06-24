package me.skymc.taboolib.common.nms;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.nms.nbt.NBTCompound;
import me.skymc.taboolib.common.nms.nbt.NBTList;
import me.skymc.taboolib.common.packet.TPacketHandler;
import me.skymc.taboolib.common.util.SimpleReflection;
import me.skymc.taboolib.nms.NMSUtils;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:42
 */
public class NMSHandlerImpl extends NMSHandler {

    private final int VERSION = TabooLib.getVersionNumber();
    private Field entityTypesField;

    static {
        SimpleReflection.saveField(NBTTagString.class);
        SimpleReflection.saveField(NBTTagDouble.class);
        SimpleReflection.saveField(NBTTagInt.class);
        SimpleReflection.saveField(NBTTagFloat.class);
        SimpleReflection.saveField(NBTTagShort.class);
        SimpleReflection.saveField(NBTTagLong.class);
        SimpleReflection.saveField(NBTTagByte.class);
        SimpleReflection.saveField(NBTTagIntArray.class);
        SimpleReflection.saveField(NBTTagByteArray.class);
        SimpleReflection.saveField(NBTTagList.class);
        SimpleReflection.saveField(NBTTagCompound.class);
    }

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
    public double[] getTPS() {
        return MinecraftServer.getServer().recentTps;
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
        } else if (TabooLib.getVersionNumber() >= 11100) {
            String name = ((net.minecraft.server.v1_12_R1.ItemStack) nmsItem).getItem().a((net.minecraft.server.v1_12_R1.ItemStack) nmsItem);
            if (itemStack.getItemMeta() instanceof PotionMeta) {
                return name.replace("item.", "") + ".effect." + ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag().getString("Potion").replaceAll("(minecraft:)?(strong_|long_)?", "");
            }
            return name + ".name";
        } else {
            String name = ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getItem().getName();
            if (itemStack.getItemMeta() instanceof PotionMeta) {
                return name.replace("item.", "") + ".effect." + ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag().getString("Potion").replaceAll("(minecraft:)?(strong_|long_)?", "");
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
    public ItemStack _NBT(ItemStack itemStack, Object compound) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        try {
            ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).setTag((net.minecraft.server.v1_8_R3.NBTTagCompound) toNBTBase((me.skymc.taboolib.common.nms.nbt.NBTBase) compound));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_8_R3.ItemStack) nmsItem);
    }

    @Override
    public Object _NBT(ItemStack itemStack) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        try {
            return ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).hasTag() ? fromNBTBase(((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag()) : new NBTCompound();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new NBTCompound();
    }

    private Object toNBTBase(me.skymc.taboolib.common.nms.nbt.NBTBase base) {
        switch (base.getType().getId()) {
            case 1:
                return new NBTTagByte(base.asByte());
            case 2:
                return new NBTTagShort(base.asShort());
            case 3:
                return new NBTTagInt(base.asInt());
            case 4:
                return new NBTTagLong(base.asLong());
            case 5:
                return new NBTTagFloat(base.asFloat());
            case 6:
                return new NBTTagDouble(base.asDouble());
            case 7:
                return new NBTTagByteArray(base.asByteArray());
            case 11:
                return new NBTTagIntArray(base.asIntArray());
            case 8:
                return new NBTTagString(base.asString());
            case 9:
                Object nmsList = new NBTTagList();
                for (me.skymc.taboolib.common.nms.nbt.NBTBase value : base.asList()) {
                    ((NBTTagList) nmsList).add((NBTBase) toNBTBase(value));
                }
                return nmsList;
            case 10:
                Object nmsTag = new net.minecraft.server.v1_8_R3.NBTTagCompound();
                for (Map.Entry<String, me.skymc.taboolib.common.nms.nbt.NBTBase> entry : base.asCompound().entrySet()) {
                    ((net.minecraft.server.v1_8_R3.NBTTagCompound) nmsTag).set(entry.getKey(), (NBTBase) toNBTBase(entry.getValue()));
                }
                return nmsTag;
        }
        return null;
    }

    private Object fromNBTBase(Object base) {
        if (base instanceof net.minecraft.server.v1_8_R3.NBTTagCompound) {
            NBTCompound nbtCompound = new NBTCompound();
            for (Map.Entry<String, net.minecraft.server.v1_12_R1.NBTBase> entry : ((Map<String, net.minecraft.server.v1_12_R1.NBTBase>) SimpleReflection.getFieldValue(NBTTagCompound.class, base, "map")).entrySet()) {
                nbtCompound.put(entry.getKey(), (me.skymc.taboolib.common.nms.nbt.NBTBase) fromNBTBase(entry.getValue()));
            }
            return nbtCompound;
        } else if (base instanceof NBTTagList) {
            NBTList nbtList = new NBTList();
            for (Object v : (List) SimpleReflection.getFieldValue(NBTTagList.class, base, "list")) {
                nbtList.add((me.skymc.taboolib.common.nms.nbt.NBTBase) fromNBTBase(v));
            }
            return nbtList;
        } else if (base instanceof NBTTagString) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagString.class, base, "data", ""));
        } else if (base instanceof NBTTagDouble) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagDouble.class, base, "data", 0D));
        } else if (base instanceof NBTTagInt) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagInt.class, base, "data", 0));
        } else if (base instanceof NBTTagFloat) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagFloat.class, base, "data", (float) 0));
        } else if (base instanceof NBTTagShort) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagShort.class, base, "data", (short) 0));
        } else if (base instanceof NBTTagLong) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagLong.class, base, "data", 0L));
        } else if (base instanceof NBTTagByte) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagByte.class, base, "data", (byte) 0D));
        } else if (base instanceof NBTTagIntArray) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagIntArray.class, base, "data", new int[0]));
        } else if (base instanceof NBTTagByteArray) {
            return new me.skymc.taboolib.common.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagByteArray.class, base, "data", new byte[0]));
        }
        return null;
    }
}
