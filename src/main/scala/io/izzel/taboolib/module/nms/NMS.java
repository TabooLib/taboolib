package io.izzel.taboolib.module.nms;

import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:38
 */
@TFunction(enable = "init")
public abstract class NMS {

    private static NMS impl;

    public static NMS handle() {
        return impl;
    }

    static void init() {
        try {
            impl = (NMS) SimpleVersionControl.createNMS("io.izzel.taboolib.module.nms.NMSImpl").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
