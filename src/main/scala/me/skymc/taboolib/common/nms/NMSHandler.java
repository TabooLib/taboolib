package me.skymc.taboolib.common.nms;

import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.common.nms.nbt.NBTCompound;
import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:38
 */
@TFunction(enable = "init")
public abstract class NMSHandler {

    private static NMSHandler handler;

    public static NMSHandler getHandler() {
        return handler;
    }

    static void init() {
        try {
            handler = (NMSHandler) SimpleVersionControl.createNMS("me.skymc.taboolib.common.nms.NMSHandlerImpl").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
