package io.izzel.taboolib.module.nms;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.module.nms.nbt.*;
import io.izzel.taboolib.module.packet.TPacketHandler;
import io.izzel.taboolib.util.Ref;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:42
 */
public class NMSImpl extends NMS {

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

    public NMSImpl() {
        if (Version.isAfter(Version.v1_13)) {
            SimpleReflection.saveField(net.minecraft.server.v1_12_R1.Entity.class);
            for (Field declaredField : SimpleReflection.getFields(net.minecraft.server.v1_12_R1.Entity.class).values()) {
                if (declaredField.getType().getSimpleName().equals("EntityTypes")) {
                    entityTypesField = declaredField;
                    break;
                }
            }
        }
        SimpleReflection.saveField(MinecraftServer.class);
    }

    @Override
    public void openBook(Player player, ItemStack book) {
        // 你妈 1.14.3 的 a() 到 1.14.4 的 openBook() 不改 nms 版本号？都是 1_14_R1？神经病吧
        Object bookItem = org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack.asNMSCopy(book);
        try {
            ((CraftPlayer) player).getHandle().a((net.minecraft.server.v1_13_R2.ItemStack) bookItem, EnumHand.MAIN_HAND);
        } catch (Throwable ignored) {
            try {
                ((org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer) player).getHandle().openBook((net.minecraft.server.v1_14_R1.ItemStack) bookItem, net.minecraft.server.v1_14_R1.EnumHand.MAIN_HAND);
            } catch (Throwable ignored2) {
            }
        }
    }

    @Override
    public boolean isRunning() {
        return !SimpleReflection.getFieldValue(MinecraftServer.class, ((CraftServer) Bukkit.getServer()).getServer(), "hasStopped", false);
    }

    @Override
    public Object toPacketPlayOutWorldParticles(Particle var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, Object var11) {
        return new net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles(CraftParticle.toNMS(var1), var2, var3, var4, var5, var6, var7, var8, var9, var10, CraftParticle.toData(var1, var11));
    }

    @Override
    public double[] getTPS() {
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public String getName(ItemStack itemStack) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (Version.isAfter(Version.v1_13)) {
            String name = ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getItem().getName();
            if (itemStack.getItemMeta() instanceof PotionMeta) {
                name += ".effect." + ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag().getString("Potion").replaceAll("minecraft:(strong_|long_)?", "");
            }
            return name;
        } else if (Version.isAfter(Version.v1_11)) {
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
        if (Version.isAfter(Version.v1_14)) {
            Object minecraftKey = net.minecraft.server.v1_14_R1.EntityTypes.getName(((org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity) entity).getHandle().getEntityType());
            return "entity.minecraft." + ((net.minecraft.server.v1_14_R1.MinecraftKey) minecraftKey).getKey();
        } else if (Version.isAfter(Version.v1_13)) {
            try {
                String name = "entity.minecraft." + IRegistry.ENTITY_TYPE.getKey((net.minecraft.server.v1_13_R2.EntityTypes<?>) Ref.getField(((org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity) entity).getHandle(), entityTypesField)).getKey();
                if (entity instanceof Villager && ((CraftVillager) entity).getCareer() != null) {
                    name += "." + String.valueOf(((CraftVillager) entity).getCareer()).toLowerCase();
                }
                return name;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return "entity.null";
        } else {
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
            } catch (Throwable ignore) {
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
        if (Version.isAfter(Version.v1_12)) {
            TPacketHandler.sendPacket(player, new net.minecraft.server.v1_12_R1.PacketPlayOutChat(new net.minecraft.server.v1_12_R1.ChatComponentText(String.valueOf(text)), ChatMessageType.GAME_INFO));
        } else {
            TPacketHandler.sendPacket(player, new PacketPlayOutChat(new ChatComponentText(String.valueOf(text)), (byte) 2));
        }
    }

    @Override
    public ItemStack _NBT(ItemStack itemStack, Object compound) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        try {
            ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).setTag((net.minecraft.server.v1_8_R3.NBTTagCompound) toNBTBase((io.izzel.taboolib.module.nms.nbt.NBTBase) compound));
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

    @Override
    public List<NBTAttribute> getBaseAttribute(ItemStack item) {
        List<NBTAttribute> list = Lists.newArrayList();
        Object nmsItem = CraftItemStack.asNMSCopy(item);
        Object attr;
        if (Version.isAfter(Version.v1_9)) {
            attr = ((net.minecraft.server.v1_12_R1.ItemStack) nmsItem).getItem().a(net.minecraft.server.v1_12_R1.EnumItemSlot.MAINHAND);
        } else {
            attr = ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getItem().i();
        }
        ((Multimap) attr).forEach((k, v) -> {
            Object nbt = net.minecraft.server.v1_12_R1.GenericAttributes.a((net.minecraft.server.v1_12_R1.AttributeModifier) v);
            list.add(new NBTAttribute(
                    new UUID(((NBTTagCompound) nbt).getLong("UUIDMost"), ((NBTTagCompound) nbt).getLong("UUIDLeast")),
                    String.valueOf(k),
                    ((NBTTagCompound) nbt).getString("Name"),
                    ((NBTTagCompound) nbt).getDouble("Amount"),
                    NBTOperation.fromIndex(((NBTTagCompound) nbt).getInt("Operation"))
            ));
        });
        return list;
    }

    @Override
    public Object toNMS(Attribute attribute) {
        SimpleReflection.checkAndSave(GenericAttributes.class);
        return SimpleReflection.getFieldValue(GenericAttributes.class, null, attribute.name());
    }

    @Override
    public Entity getEntityById(int id) {
        for (World world : Bukkit.getServer().getWorlds()) {
            net.minecraft.server.v1_13_R2.Entity entity = ((CraftWorld) world).getHandle().getEntity(id);
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }
        return null;
    }

    @Override
    public io.izzel.taboolib.module.nms.impl.Position fromBlockPosition(Object blockPosition) {
        return blockPosition instanceof net.minecraft.server.v1_12_R1.BlockPosition ? new io.izzel.taboolib.module.nms.impl.Position(((net.minecraft.server.v1_12_R1.BlockPosition) blockPosition).getX(), ((net.minecraft.server.v1_12_R1.BlockPosition) blockPosition).getY(), ((net.minecraft.server.v1_12_R1.BlockPosition) blockPosition).getZ()) : null;
    }

    @Override
    public void openSignEditor(Player player, Block block) {
        try {
            ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(new net.minecraft.server.v1_12_R1.PacketPlayOutOpenSignEditor(new net.minecraft.server.v1_12_R1.BlockPosition(block.getX(), block.getY(), block.getZ())));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Object toNBTBase(io.izzel.taboolib.module.nms.nbt.NBTBase base) {
        switch (base.getType().getId()) {
            case 1:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagByte.a(base.asByte());
                } else {
                    return new NBTTagByte(base.asByte());
                }
            case 2:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagShort.a(base.asShort());
                } else {
                    return new NBTTagShort(base.asShort());
                }
            case 3:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagInt.a(base.asInt());
                } else {
                    return new NBTTagInt(base.asInt());
                }
            case 4:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagLong.a(base.asLong());
                } else {
                    return new NBTTagLong(base.asLong());
                }
            case 5:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagFloat.a(base.asFloat());
                } else {
                    return new NBTTagFloat(base.asFloat());
                }
            case 6:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagDouble.a(base.asDouble());
                } else {
                    return new NBTTagDouble(base.asDouble());
                }
            case 7:
                return new NBTTagByteArray(base.asByteArray());
            case 11:
                return new NBTTagIntArray(base.asIntArray());
            case 8:
                if (Version.isAfter(Version.v1_15)) {
                    return net.minecraft.server.v1_15_R1.NBTTagString.a(base.asString());
                } else {
                    return new NBTTagString(base.asString());
                }
            case 9:
                Object nmsList = new NBTTagList();
                for (io.izzel.taboolib.module.nms.nbt.NBTBase value : base.asList()) {
                    // 1.14+
                    if (Version.isAfter(Version.v1_14)) {
                        ((net.minecraft.server.v1_14_R1.NBTTagList) nmsList).add(((net.minecraft.server.v1_14_R1.NBTTagList) nmsList).size(), (net.minecraft.server.v1_14_R1.NBTBase) toNBTBase(value));
                    }
                    // 1.13
                    else if (Version.isAfter(Version.v1_13)) {
                        ((net.minecraft.server.v1_13_R2.NBTTagList) nmsList).add((net.minecraft.server.v1_13_R2.NBTBase) toNBTBase(value));
                    }
                    // 1.12-
                    else {
                        ((NBTTagList) nmsList).add((NBTBase) toNBTBase(value));
                    }
                }
                return nmsList;
            case 10:
                Object nmsTag = new net.minecraft.server.v1_8_R3.NBTTagCompound();
                for (Map.Entry<String, io.izzel.taboolib.module.nms.nbt.NBTBase> entry : base.asCompound().entrySet()) {
                    ((Map) SimpleReflection.getFieldValue(NBTTagCompound.class, nmsTag, "map")).put(entry.getKey(), toNBTBase(entry.getValue()));
                }
                return nmsTag;
        }
        return null;
    }

    private Object fromNBTBase(Object base) {
        if (base instanceof net.minecraft.server.v1_8_R3.NBTTagCompound) {
            NBTCompound nbtCompound = new NBTCompound();
            for (Map.Entry<String, net.minecraft.server.v1_12_R1.NBTBase> entry : ((Map<String, net.minecraft.server.v1_12_R1.NBTBase>) SimpleReflection.getFieldValue(NBTTagCompound.class, base, "map")).entrySet()) {
                nbtCompound.put(entry.getKey(), (io.izzel.taboolib.module.nms.nbt.NBTBase) fromNBTBase(entry.getValue()));
            }
            return nbtCompound;
        } else if (base instanceof NBTTagList) {
            NBTList nbtList = new NBTList();
            for (Object v : (List) SimpleReflection.getFieldValue(NBTTagList.class, base, "list")) {
                nbtList.add((io.izzel.taboolib.module.nms.nbt.NBTBase) fromNBTBase(v));
            }
            return nbtList;
        } else if (base instanceof NBTTagString) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagString.class, base, "data", ""));
        } else if (base instanceof NBTTagDouble) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagDouble.class, base, "data", 0D));
        } else if (base instanceof NBTTagInt) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagInt.class, base, "data", 0));
        } else if (base instanceof NBTTagFloat) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagFloat.class, base, "data", (float) 0));
        } else if (base instanceof NBTTagShort) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagShort.class, base, "data", (short) 0));
        } else if (base instanceof NBTTagLong) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagLong.class, base, "data", 0L));
        } else if (base instanceof NBTTagByte) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagByte.class, base, "data", (byte) 0D));
        } else if (base instanceof NBTTagIntArray) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagIntArray.class, base, "data", new int[0]));
        } else if (base instanceof NBTTagByteArray) {
            return new io.izzel.taboolib.module.nms.nbt.NBTBase(SimpleReflection.getFieldValue(NBTTagByteArray.class, base, "data", new byte[0]));
        }
        return null;
    }

    @Override
    public boolean inBoundingBox(Entity entity, Vector vector) {
        if (Version.isAfter(Version.v1_14)) {
            return ((org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity) entity).getHandle().getBoundingBox().c(new net.minecraft.server.v1_14_R1.Vec3D(vector.getX(), vector.getY(), vector.getZ()));
        } else {
            return ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity) entity).getHandle().getBoundingBox().b(new net.minecraft.server.v1_12_R1.Vec3D(vector.getX(), vector.getY(), vector.getZ()));
        }
    }

    @Override
    public Location getLastLocation(ProjectileHitEvent event) {
        Vector vector = event.getEntity().getVelocity().normalize().multiply(0.1);
        Vector vectorEntity = event.getEntity().getLocation().toVector();
        if (event.getHitBlock() != null) {
            double i = 0;
            double length = event.getHitBlock().getLocation().add(0.5, 0.5, 0.5).distance(event.getEntity().getLocation()) * 2;
            while (i < length) {
                Location location = vectorEntity.toLocation(event.getHitBlock().getWorld());
                if (location.getBlock().getLocation().equals(event.getHitBlock().getLocation())) {
                    return location;
                }
                vectorEntity.add(vector);
                i += 0.1;
            }
        }
        if (event.getHitEntity() != null) {
            double i = 0;
            double length = event.getHitEntity().getLocation().distance(event.getEntity().getLocation()) * 2;
            while (i < length) {
                if (inBoundingBox(event.getHitEntity(), vectorEntity)) {
                    return vectorEntity.toLocation(event.getHitEntity().getWorld());
                }
                vectorEntity.add(vector);
                i += 0.1;
            }
        }
        return null;
    }
}
