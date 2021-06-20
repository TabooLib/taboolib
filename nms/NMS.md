# NMS
快捷使用NMS中部分常用方法。

```java
/**
 * NMS 工具
 *
 * @author 坏黑
 * @since 2018-11-09 14:38
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class NMS {

    @TInject(asm = "io.izzel.taboolib.module.nms.NMSImpl")
    private static NMS impl;

    /**
     * 获取工具实例
     *
     * @return NMS
     */
    @NotNull
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
     *
     * @return boolean
     */
    abstract public boolean isRunning();

    @NotNull
    abstract public Object toPacketPlayOutWorldParticles(Particle var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, Object var11);

    /**
     * @return 服务端 TPS 运行状态
     */
    abstract public double[] getTPS();

    @NotNull
    abstract public String getName(ItemStack itemStack);

    @NotNull
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
     * @return NMS NBT
     */
    @Deprecated
    abstract public Object _NBT(ItemStack itemStack);

    /**
     * 写入物品的 NBT 结构
     * 命名存在误导，不建议使用
     *
     * @param itemStack 物品
     * @param compound  NBT 结构
     * @return ItemStack
     */
    @Deprecated
    abstract public ItemStack _NBT(ItemStack itemStack, Object compound);

    /**
     * 获取物品的 NBT 结构
     *
     * @param itemStack 物品实例
     * @return NBTCompound
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
     * @return ItemStack
     */
    @NotNull
    public ItemStack saveNBT(ItemStack itemStack, NBTCompound compound) {
        return _NBT(itemStack, compound);
    }

    /**
     * 获取物品的所有属性
     *
     * @param item 物品实例
     * @return 属性
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
     * @return ItemStack
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
     * @return 物品基础属性
     */
    @NotNull
    abstract public List<NBTAttribute> getBaseAttribute(ItemStack item);

    /**
     * 将 Attribute 映射类转换为 nms 对应属性类
     *
     * @param attribute Attribute 映射累
     * @return NMS Attribute
     */
    @Nullable
    abstract public Object toNMS(Attribute attribute);

    /**
     * 通过实体 id 获取实体对象
     *
     * @param id id
     * @return Entity
     */
    @Nullable
    abstract public Entity getEntityById(int id);

    /**
     * @param blockPosition NMS BlockPosition
     * @return 将 nms BlockPosition 类转换为 {@link Position} 映射类
     */
    @NotNull
    abstract public Position fromBlockPosition(Object blockPosition);

    /**
     * @param blockPosition {@link Position}
     * @return 将 {@link Position} 映射类转换为 nms BlockPosition 类
     */
    @NotNull
    abstract public Object toBlockPosition(Position blockPosition);

    /**
     * 打开牌子编辑界面
     *
     * @param player 玩家实例
     * @param block  方块实例
     */
    abstract public void openSignEditor(Player player, Block block);

    /**
     * 判断某个坐标点是否在实体碰撞箱中
     *
     * @param entity 实体对象
     * @param vector 向量
     * @return boolean
     */
    abstract public boolean inBoundingBox(Entity entity, Vector vector);

    /**
     * 获取 ProjectileHitEvent 事件中箭矢的最终位置
     *
     * @param event 事件
     * @return Location
     */
    @Nullable
    abstract public Location getLastLocation(ProjectileHitEvent event);

    /**
     * 向玩家发送实体移除数据包
     *
     * @param player 玩家
     * @param entity 实体id
     */
    abstract public void sendPacketEntityDestroy(Player player, int entity);

    /**
     * 向玩家发送实体传送数据包
     *
     * @param player   玩家
     * @param entity   实体id
     * @param location 坐标
     */
    abstract public void sendPacketEntityTeleport(Player player, int entity, Location location);

    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    abstract public <T extends Entity> T spawn(Location location, Class<T> entity, Consumer<T> e);

    @NotNull
    abstract public Object ofChatComponentText(String source);

    @Nullable
    abstract public Class<?> asNMS(String name);

    @Nullable
    abstract public Object asEntityType(String name);

    /**
     * 创造真实世界光照
     *
     * @param block      方块
     * @param lightType  光照类型
     * @param lightLevel 光照等级
     * @return boolean
     */
    abstract public boolean createLight(Block block, Type lightType, int lightLevel);

    /**
     * 删除光照
     *
     * @param block     方块
     * @param lightType 光照等级
     * @return boolean
     */
    abstract public boolean deleteLight(Block block, Type lightType);

    /**
     * 设置真实光照等级
     *
     * @param block      方块
     * @param lightType  光照类型
     * @param lightLevel 光照等级
     */
    abstract public void setRawLightLevel(Block block, Type lightType, int lightLevel);

    /**
     * 获取真实光照等级
     *
     * @param block     方块
     * @param lightType 光照类型
     * @return int
     */
    abstract public int getRawLightLevel(Block block, Type lightType);

    /**
     * 重新计算光照
     *
     * @param block     方块
     * @param lightType 光照类型
     */
    abstract public void recalculate(Block block, Type lightType);

    /**
     * 更新区块光照
     *
     * @param chunk 区块
     */
    abstract public void update(Chunk chunk);

    /**
     * 更新方块周围的光照
     *
     * @param block      方块
     * @param lightType  光照类型
     * @param lightLevel 光照等级
     */
    abstract public void recalculateAround(Block block, Type lightType, int lightLevel);

    /**
     * 获取附魔内部名称
     *
     * @param enchantment 附魔
     * @return String
     */
    @NotNull
    abstract public String getEnchantmentKey(Enchantment enchantment);

    /**
     * 获取药水效果内部名称
     *
     * @param potionEffectType 药水效果
     * @return String
     */
    @NotNull
    abstract public String getPotionEffectTypeKey(PotionEffectType potionEffectType);

    @NotNull
    abstract public CommandDispatcher<?> getDispatcher();

    @NotNull
    abstract public CommandSender getBukkitSender(Object commandWrapperListener);

    @NotNull
    abstract public Object getWrapper(Command command);

    @NotNull
    abstract public Class<?> getArgumentRegistryClass();

    @NotNull
    abstract public Class<?> getMinecraftKeyClass();

    @NotNull
    abstract public Object createMinecraftKey(NamespacedKey key);
}
```