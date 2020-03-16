package io.izzel.taboolib.module.nms;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.nms.impl.Position;
import io.izzel.taboolib.module.nms.nbt.Attribute;
import io.izzel.taboolib.module.nms.nbt.NBTAttribute;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.module.nms.nbt.NBTList;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:38
 */
public abstract class NMS {

    @TInject(asm = "io.izzel.taboolib.module.nms.NMSImpl")
    private static NMS impl;

    public static NMS handle() {
        return impl;
    }

    abstract public void openBook(Player player, ItemStack book);

    abstract public boolean isRunning();

    abstract public Object toPacketPlayOutWorldParticles(Particle var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, Object var11);

    abstract public double[] getTPS();

    abstract public String getName(ItemStack itemStack);

    abstract public String getName(Entity entity);

    abstract public void sendTitle(Player player, String title, int titleFadein, int titleStay, int titleFadeout, String subtitle, int subtitleFadein, int subtitleStay, int subtitleFadeout);

    abstract public void sendActionBar(Player player, String text);

    abstract public Object _NBT(ItemStack itemStack);

    abstract public ItemStack _NBT(ItemStack itemStack, Object compound);

    public NBTCompound loadNBT(ItemStack itemStack) {
        return (NBTCompound) _NBT(itemStack);
    }

    public ItemStack saveNBT(ItemStack itemStack, NBTCompound compound) {
        return _NBT(itemStack, compound);
    }

    public List<NBTAttribute> getAttribute(ItemStack item) {
        NBTCompound nbt = loadNBT(item);
        return !nbt.containsKey("AttributeModifiers") ? Lists.newCopyOnWriteArrayList() : nbt.get("AttributeModifiers").asList().stream().map(element -> NBTAttribute.fromNBT(element.asCompound())).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public ItemStack setAttribute(ItemStack item, List<NBTAttribute> attributes) {
        NBTCompound nbt = loadNBT(item);
        nbt.put("AttributeModifiers", attributes.stream().map(NBTAttribute::toNBT).collect(Collectors.toCollection(NBTList::new)));
        return saveNBT(item, nbt);
    }

    abstract public List<NBTAttribute> getBaseAttribute(ItemStack item);

    abstract public Object toNMS(Attribute attribute);

    abstract public Entity getEntityById(int id);

    abstract public Position fromBlockPosition(Object blockPosition);

    abstract public void openSignEditor(Player player, Block block);

    abstract public boolean inBoundingBox(Entity entity, Vector vector);

    abstract public Location getLastLocation(ProjectileHitEvent event);

    abstract public void sendPacketEntityDestroy(Player player, int entity);

    abstract public void sendPacketEntityTeleport(Player player, int entity, Location location);

    abstract public <T extends Entity> T spawn(Location location, Class<T> entity, Consumer<T> e);

    abstract public Object ofChatComponentText(String source);

    abstract public Class<?> asNMS(String name);

    abstract public Object asEntityType(String name);

}
