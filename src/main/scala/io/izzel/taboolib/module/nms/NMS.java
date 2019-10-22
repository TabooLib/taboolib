package io.izzel.taboolib.module.nms;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.nms.nbt.NBTAttribute;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.module.nms.nbt.NBTList;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
}
