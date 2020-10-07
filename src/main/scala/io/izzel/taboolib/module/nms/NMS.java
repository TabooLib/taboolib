package io.izzel.taboolib.module.nms;

import com.google.common.collect.Lists;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.nms.impl.Position;
import io.izzel.taboolib.module.nms.impl.Type;
import io.izzel.taboolib.module.nms.nbt.Attribute;
import io.izzel.taboolib.module.nms.nbt.NBTAttribute;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.module.nms.nbt.NBTList;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * NMS 工具
 *
 * @Author 坏黑
 * @Since 2018-11-09 14:38
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class NMS {

    @TInject(asm = "io.izzel.taboolib.module.nms.NMSImpl")
    private static NMS impl;

    /**
     * 获取工具实例
     */
    public static NMS handle() {
        return impl;
    }

    /**
     * 打开书本界面
     *
     * @param player 玩家实例
     * @param book   物品实例（需要成书类型）
     */
    abstract public void openBook(Player player, ItemStack book);

    /**
     * 服务端是否正在运行
     * 当服务端开始关闭时该方法将会返回 false
     */
    abstract public boolean isRunning();

    abstract public Object toPacketPlayOutWorldParticles(Particle var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, Object var11);

    /**
     * 获取服务端 TPS 运行状态
     */
    abstract public double[] getTPS();

    abstract public String getName(ItemStack itemStack);

    abstract public String getName(Entity entity);

    /**
     * 发送标题信息
     *
     * @param player          玩家实例
     * @param title           标题
     * @param titleFadein     淡入
     * @param titleStay       停留
     * @param titleFadeout    淡出
     * @param subtitle        小标题
     * @param subtitleFadein  小标题淡入
     * @param subtitleStay    小标题停留
     * @param subtitleFadeout 小标题淡出
     */
    abstract public void sendTitle(Player player, String title, int titleFadein, int titleStay, int titleFadeout, String subtitle, int subtitleFadein, int subtitleStay, int subtitleFadeout);

    /**
     * 发送动作栏信息
     *
     * @param player 玩家实例
     * @param text   文本
     */
    abstract public void sendActionBar(Player player, String text);

    /**
     * 获取物品的 NBT 结构
     * 命名存在误导，不建议使用
     *
     * @param itemStack 物品
     */
    @Deprecated
    abstract public Object _NBT(ItemStack itemStack);

    /**
     * 写入物品的 NBT 结构
     * 命名存在误导，不建议使用
     *
     * @param itemStack 物品
     * @param compound  NBT 结构
     */
    @Deprecated
    abstract public ItemStack _NBT(ItemStack itemStack, Object compound);

    /**
     * 获取物品的 NBT 结构
     *
     * @param itemStack 物品实例
     */
    @NotNull
    public NBTCompound loadNBT(ItemStack itemStack) {
        return (NBTCompound) _NBT(itemStack);
    }

    /**
     * 写入物品的 NBT 结构
     *
     * @param itemStack 物品实例
     * @param compound  NBT 结构
     */
    @NotNull
    public ItemStack saveNBT(ItemStack itemStack, NBTCompound compound) {
        return _NBT(itemStack, compound);
    }

    /**
     * 获取物品的所有属性
     *
     * @param item 物品实例
     */
    @NotNull
    public List<NBTAttribute> getAttribute(ItemStack item) {
        NBTCompound nbt = loadNBT(item);
        return !nbt.containsKey("AttributeModifiers") ? Lists.newCopyOnWriteArrayList() : nbt.get("AttributeModifiers").asList().stream().map(element -> NBTAttribute.fromNBT(element.asCompound())).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    /**
     * 设置物品的所有属性
     *
     * @param item       物品实例
     * @param attributes 属性实例
     */
    @NotNull
    public ItemStack setAttribute(ItemStack item, List<NBTAttribute> attributes) {
        NBTCompound nbt = loadNBT(item);
        nbt.put("AttributeModifiers", attributes.stream().map(NBTAttribute::toNBT).collect(Collectors.toCollection(NBTList::new)));
        return saveNBT(item, nbt);
    }

    /**
     * 获取物品的基础属性
     * 这个属性不会存在与物品上，是由 Minecraft 提供的默认属性，例如钻石剑的 7 点攻击力。
     *
     * @param item 物品实例
     */
    @NotNull
    abstract public List<NBTAttribute> getBaseAttribute(ItemStack item);

    abstract public Object toNMS(Attribute attribute);

    abstract public Entity getEntityById(int id);

    abstract public Position fromBlockPosition(Object blockPosition);

    abstract public Object toBlockPosition(Position blockPosition);

    abstract public void openSignEditor(Player player, Block block);

    abstract public boolean inBoundingBox(Entity entity, Vector vector);

    abstract public Location getLastLocation(ProjectileHitEvent event);

    abstract public void sendPacketEntityDestroy(Player player, int entity);

    abstract public void sendPacketEntityTeleport(Player player, int entity, Location location);

    abstract public <T extends Entity> T spawn(Location location, Class<T> entity, Consumer<T> e);

    abstract public Object ofChatComponentText(String source);

    abstract public Class<?> asNMS(String name);

    abstract public Object asEntityType(String name);

    abstract public boolean createLight(Block block, Type lightType, int lightLevel);

    abstract public boolean deleteLight(Block block, Type lightType);

    abstract public void setRawLightLevel(Block block, Type lightType, int lightLevel);

    abstract public int getRawLightLevel(Block block, Type lightType);

    abstract public void recalculate(Block block, Type lightType);

    abstract public void update(Chunk chunk);

}
