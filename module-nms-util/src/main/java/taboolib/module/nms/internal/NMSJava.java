package taboolib.module.nms.internal;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import taboolib.module.nms.ItemTag;
import taboolib.module.nms.ItemTagData;
import taboolib.module.nms.type.LightType;

import java.util.function.Consumer;

/**
 * TabooLib
 * taboolib.module.nms.NMS
 *
 * @author sky
 * @since 2021/6/18 8:54 下午
 */
public abstract class NMSJava {

    abstract public void sendPacket(Player player, Object packet);

    @NotNull
    abstract public String getKey(ItemStack itemStack);

    @NotNull
    abstract public String getName(ItemStack itemStack);

    @NotNull
    abstract public String getName(Entity entity);

    @NotNull
    abstract public ItemTag getItemTag(ItemStack itemStack);

    @NotNull
    abstract public ItemStack setItemTag(ItemStack itemStack, ItemTag compound);

    @NotNull
    abstract public String itemTagToString(ItemTagData itemTag);

    abstract public Object getEntityType(String name);

    abstract public <T extends Entity> T spawnEntity(Location location, Class<T> entity, Consumer<T> e);

    abstract public boolean createLight(Block block, LightType lightType, int lightLevel);

    abstract public boolean deleteLight(Block block, LightType lightType);

    abstract public int getRawLightLevel(Block block, LightType lightType);

    abstract public void setRawLightLevel(Block block, LightType lightType, int lightLevel);

    abstract public void recalculateLight(Block block, LightType lightType);

    abstract public void recalculateLightAround(Block block, LightType lightType, int lightLevel);

    abstract public void updateLight(Chunk chunk);

    abstract public String getEnchantmentKey(Enchantment enchantment);

    abstract public String getPotionEffectTypeKey(PotionEffectType potionEffectType);
}
