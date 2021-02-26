package io.izzel.taboolib.module.tellraw.internal;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.module.packet.TPacketHandler;
import io.izzel.taboolib.module.tellraw.TellrawVersion;
import io.izzel.taboolib.util.item.Items;
import net.minecraft.server.v1_16_R1.ChatMessageType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author 坏黑
 * @since 2018-11-07 22:54
 */
@SuppressWarnings({"CastCanBeRemovedNarrowingVariableType", "unchecked", "rawtypes"})
public class InternalTellraw implements AbstractTellraw {

    private final boolean v11600 = Version.isAfter(Version.v1_16);

    public InternalTellraw() {
        SimpleReflection.saveField(NBTTagCompound.class, "map");
        SimpleReflection.saveField(NBTTagList.class, "list");
    }

    @Override
    public void sendRawMessage(Player player, String rawMessage) {
        if (v11600) {
            TPacketHandler.sendPacket(player, new net.minecraft.server.v1_16_R1.PacketPlayOutChat(net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer.a(rawMessage), ChatMessageType.CHAT, UUID.randomUUID()));
        } else {
            TPacketHandler.sendPacket(player, new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(rawMessage)));
        }
    }

    @Override
    public String getItemComponent(ItemStack itemStack) {
        return getItemComponent(itemStack, TellrawVersion.CURRENT_VERSION);
    }

    @Override
    public String getItemComponent(ItemStack itemStack, TellrawVersion version) {
        return nbtToString(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()), version);
    }

    @Override
    public ItemStack optimizeNBT(ItemStack itemStack, List<String> nbtWhitelist) {
        Object nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (Items.nonNull(itemStack) && ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).hasTag()) {
            Object nbtTag = new NBTTagCompound();
            Map<String, NBTBase> mapNew =  (Map) SimpleReflection.getFieldValue(NBTTagCompound.class, nbtTag, "map");
            Map<String, NBTBase> mapOrigin = (Map) SimpleReflection.getFieldValue(NBTTagCompound.class, ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).getTag(), "map");
            for (Map.Entry<String, NBTBase> entry : mapOrigin.entrySet()) {
                if (nbtWhitelist.contains(entry.getKey())) {
                    mapNew.put(entry.getKey(), entry.getValue());
                }
            }
            ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).setTag((NBTTagCompound) nbtTag);
           return  CraftItemStack.asBukkitCopy(((net.minecraft.server.v1_8_R3.ItemStack) nmsItem));
        }
        return itemStack;
    }

    @Override
    public ItemStack optimizeShulkerBox(ItemStack item) {
        try {
            if (item.getType().name().endsWith("SHULKER_BOX")) {
                ItemStack itemClone = item.clone();
                BlockStateMeta blockStateMeta = (BlockStateMeta) itemClone.getItemMeta();
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                ItemStack[] contents = shulkerBox.getInventory().getContents();
                ItemStack[] contentsClone = new ItemStack[contents.length];
                for (int i = 0; i < contents.length; i++) {
                    ItemStack content = contents[i];
                    if (!Items.isNull(content)) {
                        ItemStack contentClone = new ItemStack(Material.STONE, content.getAmount(), content.getDurability());
                        if (content.getItemMeta().hasDisplayName()) {
                            ItemMeta itemMeta = contentClone.getItemMeta();
                            itemMeta.setDisplayName(content.getItemMeta().getDisplayName());
                            contentClone.setItemMeta(itemMeta);
                        }
                        contentsClone[i] = contentClone;
                    }
                }
                shulkerBox.getInventory().setContents(contentsClone);
                blockStateMeta.setBlockState(shulkerBox);
                itemClone.setItemMeta(blockStateMeta);
                return itemClone;
            } else if (item.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta) item.getItemMeta()).getBlockState() instanceof InventoryHolder) {
                ItemStack itemClone = item.clone();
                BlockStateMeta blockStateMeta = (BlockStateMeta) itemClone.getItemMeta();
                InventoryHolder inventoryHolder = (InventoryHolder) blockStateMeta.getBlockState();
                inventoryHolder.getInventory().clear();
                blockStateMeta.setBlockState((org.bukkit.block.BlockState) inventoryHolder);
                itemClone.setItemMeta(blockStateMeta);
                return itemClone;
            }
        } catch (Throwable ignored) {
        }
        return item;
    }

    private String nbtToString(Object nbtTagCompound, TellrawVersion version) {
        StringBuilder builder = new StringBuilder("{");
        Map map = (Map) SimpleReflection.getFieldValue(NBTTagCompound.class, nbtTagCompound, "map");
        int index = 0;
        for (Object nbtBaseEntry : map.entrySet()) {
            if (index++ != 0) {
                builder.append(",");
            }
            Object value = ((Map.Entry) nbtBaseEntry).getValue();
            if (value instanceof NBTTagList ) {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(nbtListToString(value, version));
            } else if (value instanceof NBTTagCompound) {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(nbtToString(value, version));
            } else {
                builder.append(((Map.Entry) nbtBaseEntry).getKey()).append(":").append(value);
            }
        }
        return builder.append('}').toString();
    }

    private String nbtListToString(Object nbtTagList, TellrawVersion version) {
        StringBuilder builder = new StringBuilder("[");
        List list = (List) SimpleReflection.getFieldValue(NBTTagList.class, nbtTagList, "list");
        for (int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                builder.append(',');
            }
            if (version == TellrawVersion.HIGH_VERSION || (Version.isAfter(Version.v1_12) && version == TellrawVersion.CURRENT_VERSION)) {
                builder.append(list.get(i));
            } else {
                builder.append(i).append(':').append(list.get(i));
            }
        }
        return builder.append(']').toString();
    }
}
