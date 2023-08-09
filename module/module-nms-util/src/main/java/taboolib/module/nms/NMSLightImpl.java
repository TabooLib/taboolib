package taboolib.module.nms;

import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EnumSkyBlock;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_16_R1.WorldDataServer;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.Reflex;
import org.tabooproject.reflex.UnsafeAccess;
import taboolib.common.platform.function.IOKt;
import taboolib.module.nms.type.LightType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static taboolib.module.nms.MinecraftServerUtilKt.nmsClass;
import static taboolib.module.nms.MinecraftServerUtilKt.sendPacket;

/**
 * TabooLib
 * taboolib.module.nms.NMS
 *
 * @author sky
 * @since 2021/6/18 8:54 下午
 */
@SuppressWarnings("ALL")
public class NMSLightImpl extends NMSLight {

    private Constructor packetPlayOutLightUpdateConstructor;

    public NMSLightImpl() {
        try {
            // 1.20+
            if (MinecraftVersion.INSTANCE.isHigherOrEqual(MinecraftVersion.V1_20)) {
                packetPlayOutLightUpdateConstructor = net.minecraft.server.v1_16_R1.PacketPlayOutLightUpdate.class.getDeclaredConstructor(
                        net.minecraft.server.v1_16_R1.ChunkCoordIntPair.class,
                        nmsClass("LevelLightEngine"), // class file has wrong version 61.0, should be 52.0
                        BitSet.class,
                        BitSet.class
                );
            } else if (MinecraftVersion.INSTANCE.isHigherOrEqual(MinecraftVersion.V1_17)) {
                packetPlayOutLightUpdateConstructor = net.minecraft.server.v1_16_R1.PacketPlayOutLightUpdate.class.getDeclaredConstructor(
                        net.minecraft.server.v1_16_R1.ChunkCoordIntPair.class,
                        net.minecraft.server.v1_16_R1.LightEngine.class,
                        BitSet.class,
                        BitSet.class,
                        Boolean.TYPE
                );
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createLight(Block block, LightType lightType, int lightLevel) {
        int level = getRawLightLevel(block, lightType);
        setRawLightLevel(block, lightType, lightLevel);
        recalculateLightAround(block, lightType, lightLevel);
        return getRawLightLevel(block, lightType) >= level;
    }

    @Override
    public boolean deleteLight(Block block, LightType lightType) {
        int level = getRawLightLevel(block, lightType);
        setRawLightLevel(block, lightType, 0);
        recalculateLightAround(block, lightType, level);
        return getRawLightLevel(block, lightType) != level;
    }

    @Override
    public int getRawLightLevel(Block block, LightType lightType) {
        Object world = ((CraftWorld) block.getWorld()).getHandle();
        Object position = new net.minecraft.server.v1_15_R1.BlockPosition(block.getX(), block.getY(), block.getZ());
        if (MinecraftVersion.INSTANCE.getMajor() >= 4) {
            if (lightType == LightType.BLOCK) {
                return ((net.minecraft.server.v1_13_R2.WorldServer) world).getBrightness(net.minecraft.server.v1_13_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_13_R2.BlockPosition) position);
            } else if (lightType == LightType.SKY) {
                return ((net.minecraft.server.v1_13_R2.WorldServer) world).getBrightness(net.minecraft.server.v1_13_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_13_R2.BlockPosition) position);
            } else {
                return ((net.minecraft.server.v1_13_R2.WorldServer) world).getLightLevel((net.minecraft.server.v1_13_R2.BlockPosition) position);
            }
        } else {
            Object chunk = ((net.minecraft.server.v1_9_R2.WorldServer) world).getChunkAt(block.getChunk().getX(), block.getChunk().getZ());
            if (lightType == LightType.BLOCK) {
                return ((net.minecraft.server.v1_9_R2.Chunk) chunk).getBrightness(net.minecraft.server.v1_9_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_9_R2.BlockPosition) position);
            } else if (lightType == LightType.SKY) {
                return ((net.minecraft.server.v1_9_R2.Chunk) world).getBrightness(net.minecraft.server.v1_9_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_9_R2.BlockPosition) position);
            } else {
                return 15;
            }
        }
    }

    @Override
    public void setRawLightLevel(Block block, LightType lightType, int lightLevel) {
        int level = Math.max(Math.min(lightLevel, 15), 0);
        Object world = ((CraftWorld) block.getWorld()).getHandle();
        Object position = new net.minecraft.server.v1_15_R1.BlockPosition(block.getX(), block.getY(), block.getZ());
        if (MinecraftVersion.INSTANCE.getMajor() >= 6) {
            syncLight(((net.minecraft.server.v1_14_R1.WorldServer) world).getChunkProvider().getLightEngine(), lightEngine -> {
                if (lightType == LightType.BLOCK) {
                    Object lightEngineLayer = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.BLOCK);
                    setRawLightLevelBlock(level, position, lightEngineLayer);
                } else if (lightType == LightType.SKY) {
                    Object lightEngineLayer = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.SKY);
                    setRawLightLevelSky(level, position, lightEngineLayer);
                } else {
                    Object lightEngineLayer1 = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.BLOCK);
                    Object lightEngineLayer2 = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.SKY);
                    setRawLightLevelBlock(level, position, lightEngineLayer1);
                    setRawLightLevelSky(level, position, lightEngineLayer2);
                }
            });
        } else {
            if (lightType == LightType.BLOCK) {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).a(net.minecraft.server.v1_13_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_13_R2.BlockPosition) position, level);
            } else if (lightType == LightType.SKY) {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).a(net.minecraft.server.v1_13_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_13_R2.BlockPosition) position, level);
            } else {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).a(net.minecraft.server.v1_13_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_13_R2.BlockPosition) position, level);
                ((net.minecraft.server.v1_13_R2.WorldServer) world).a(net.minecraft.server.v1_13_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_13_R2.BlockPosition) position, level);
            }
        }
    }

    @Override
    public void recalculateLight(Block block, LightType lightType) {
        Object world = ((CraftWorld) block.getWorld()).getHandle();
        Object position = new net.minecraft.server.v1_15_R1.BlockPosition(block.getX(), block.getY(), block.getZ());
        if (MinecraftVersion.INSTANCE.getMajor() >= 9) {
            Object lightEngine = ((net.minecraft.server.v1_14_R1.WorldServer) world).getChunkProvider().getLightEngine();
            // 类文件具有错误的版本 60.0, 应为 52.0
            if ((boolean) Reflex.Companion.invokeMethod(lightEngine, "hasLightWork", new Object[0], false, true, true)) {
                syncLight(lightEngine, e -> {
                    if (lightType == LightType.BLOCK) {
                        ((LightEngineLayer) ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(EnumSkyBlock.BLOCK)).a(Integer.MAX_VALUE, true, true);
                    } else if (lightType == LightType.SKY) {
                        ((LightEngineLayer) ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(EnumSkyBlock.SKY)).a(Integer.MAX_VALUE, true, true);
                    } else {
                        Object b = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(EnumSkyBlock.BLOCK);
                        Object s = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(EnumSkyBlock.SKY);
                        int maxUpdateCount = Integer.MAX_VALUE;
                        int integer4 = maxUpdateCount / 2;
                        int integer5 = ((LightEngineLayer) b).a(integer4, true, true);
                        int integer6 = maxUpdateCount - integer4 + integer5;
                        int integer7 = ((LightEngineLayer) s).a(integer6, true, true);
                        if (integer5 == 0 && integer7 > 0) {
                            ((LightEngineLayer) b).a(integer7, true, true);
                        }
                    }
                });
            }
        } else if (MinecraftVersion.INSTANCE.getMajor() >= 6) {
            Object lightEngine = ((net.minecraft.server.v1_14_R1.WorldServer) world).getChunkProvider().getLightEngine();
            if (((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a()) {
                syncLight(lightEngine, e -> {
                    if (lightType == LightType.BLOCK) {
                        ((LightEngineLayer) ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.BLOCK)).a(Integer.MAX_VALUE, true, true);
                    } else if (lightType == LightType.SKY) {
                        ((LightEngineLayer) ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.SKY)).a(Integer.MAX_VALUE, true, true);
                    } else {
                        Object b = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.BLOCK);
                        Object s = ((net.minecraft.server.v1_14_R1.LightEngineThreaded) lightEngine).a(net.minecraft.server.v1_14_R1.EnumSkyBlock.SKY);
                        int maxUpdateCount = Integer.MAX_VALUE;
                        int integer4 = maxUpdateCount / 2;
                        int integer5 = ((LightEngineLayer) b).a(integer4, true, true);
                        int integer6 = maxUpdateCount - integer4 + integer5;
                        int integer7 = ((LightEngineLayer) s).a(integer6, true, true);
                        if (integer5 == 0 && integer7 > 0) {
                            ((LightEngineLayer) b).a(integer7, true, true);
                        }
                    }
                });
            }
        } else {
            if (lightType == LightType.SKY) {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).c(net.minecraft.server.v1_13_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_13_R2.BlockPosition) position);
            } else if (lightType == LightType.BLOCK) {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).c(net.minecraft.server.v1_13_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_13_R2.BlockPosition) position);
            } else {
                ((net.minecraft.server.v1_13_R2.WorldServer) world).c(net.minecraft.server.v1_13_R2.EnumSkyBlock.SKY, (net.minecraft.server.v1_13_R2.BlockPosition) position);
                ((net.minecraft.server.v1_13_R2.WorldServer) world).c(net.minecraft.server.v1_13_R2.EnumSkyBlock.BLOCK, (net.minecraft.server.v1_13_R2.BlockPosition) position);
            }
        }
    }

    @Override
    public void recalculateLightAround(Block block, LightType lightType, int lightLevel) {
        // 不能重新计算光源方块 否则光就没了
        if (getRawLightLevel(block.getRelative(1, 0, 0), lightType) < lightLevel) {
            recalculateLight(block.getRelative(1, 0, 0), lightType);
        }
        if (getRawLightLevel(block.getRelative(-1, 0, 0), lightType) < lightLevel) {
            recalculateLight(block.getRelative(-1, 0, 0), lightType);
        }
        if (getRawLightLevel(block.getRelative(0, 1, 0), lightType) < lightLevel) {
            recalculateLight(block.getRelative(0, 1, 0), lightType);
        }
        if (getRawLightLevel(block.getRelative(0, -1, 0), lightType) < lightLevel) {
            recalculateLight(block.getRelative(0, -1, 0), lightType);
        }
        if (getRawLightLevel(block.getRelative(0, 0, 1), lightType) < lightLevel) {
            recalculateLight(block.getRelative(0, 0, 1), lightType);
        }
        if (getRawLightLevel(block.getRelative(0, 0, -1), lightType) < lightLevel) {
            recalculateLight(block.getRelative(0, 0, -1), lightType);
        }
    }

    @Override
    public void updateLight(@NotNull Chunk chunk, @NotNull Collection<? extends Player> viewers) {
        for (Player player : viewers) {
            Object human = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle();
            Object chunk1 = ((CraftWorld) player.getWorld()).getHandle().getChunkAt(chunk.getX(), chunk.getZ());
            Object chunk2 = ((net.minecraft.server.v1_8_R3.EntityPlayer) human).getWorld().getChunkAtWorldCoords(((net.minecraft.server.v1_8_R3.EntityPlayer) human).getChunkCoordinates());
            if (distance(chunk2, chunk1) < distance(human)) {
                if (MinecraftVersion.INSTANCE.getMajor() >= 8) {
                    sendPacket(player, new net.minecraft.server.v1_16_R1.PacketPlayOutLightUpdate(((net.minecraft.server.v1_16_R1.Chunk) chunk1).getPos(), ((net.minecraft.server.v1_16_R1.Chunk) chunk1).getWorld().getChunkProvider().getLightEngine(), true));
                } else if (MinecraftVersion.INSTANCE.getMajor() >= 6) {
                    sendPacket(player, new PacketPlayOutLightUpdate(((net.minecraft.server.v1_14_R1.Chunk) chunk1).getPos(), ((net.minecraft.server.v1_14_R1.Chunk) chunk1).e()));
                } else {
                    sendPacket(player, new net.minecraft.server.v1_14_R1.PacketPlayOutMapChunk((net.minecraft.server.v1_14_R1.Chunk) chunk1, 0x1ffff));
                }
            }
        }
    }

    @Override
    public void updateLightUniversal(@NotNull Block block, @NotNull LightType lightType, @NotNull Collection<? extends Player> viewers) {
        Chunk chunk = block.getChunk();
        for (Player player : viewers) {
            Object human = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle();
            Object chunk1 = ((CraftWorld) player.getWorld()).getHandle().getChunkAt(chunk.getX(), chunk.getZ());
            Object chunk2 = ((net.minecraft.server.v1_8_R3.EntityPlayer) human).getWorld().getChunkAtWorldCoords(((net.minecraft.server.v1_8_R3.EntityPlayer) human).getChunkCoordinates());
            if (distance(chunk2, chunk1) < distance(human)) {
                net.minecraft.server.v1_16_R1.IChunkProvider chunkProvider = ((net.minecraft.server.v1_16_R1.Chunk) chunk1).getWorld().getChunkProvider();
                Object[] params = {net.minecraft.server.v1_16_R1.ChunkCoordIntPair.pair(chunk.getX(), chunk.getZ())};
                net.minecraft.server.v1_16_R1.PlayerChunk playerChunk = Reflex.Companion.invokeMethod(chunkProvider, "getChunk", params, false, true, true);
                BitSet skyChangedLightSectionFilter = new BitSet();
                BitSet blockChangedLightSectionFilter = new BitSet();
                if (lightType == LightType.BLOCK) {
                    blockChangedLightSectionFilter.set((block.getY() >> 4) + 1);
                } else if (lightType == LightType.SKY) {
                    skyChangedLightSectionFilter.set((block.getY() >> 4) + 1);
                } else {
                    blockChangedLightSectionFilter.set((block.getY() >> 4) + 1);
                    skyChangedLightSectionFilter.set((block.getY() >> 4) + 1);
                }
                try {
                    sendPacket(player, packetPlayOutLightUpdateConstructor.newInstance(
                            ((net.minecraft.server.v1_16_R1.Chunk) chunk1).getPos(),
                            ((net.minecraft.server.v1_16_R1.Chunk) chunk1).getWorld().getChunkProvider().getLightEngine(),
                            skyChangedLightSectionFilter,
                            blockChangedLightSectionFilter,
                            true
                    ));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int distance(Object player) {
        int viewDistance = Bukkit.getViewDistance();
        try {
            int playerViewDistance = ((net.minecraft.server.v1_14_R1.EntityPlayer) player).clientViewDistance;
            if (playerViewDistance < viewDistance) {
                viewDistance = playerViewDistance;
            }
        } catch (Throwable ignored) {
        }
        return viewDistance;
    }

    private int distance(Object from, Object to) {
        if (MinecraftVersion.INSTANCE.getMajor() >= 8) {
            String name1 = ((WorldDataServer) ((net.minecraft.server.v1_16_R1.Chunk) from).getWorld().getWorldData()).getName();
            String name2 = ((WorldDataServer) ((net.minecraft.server.v1_16_R1.Chunk) to).getWorld().getWorldData()).getName();
            if (!name1.equals(name2)) {
                return 100;
            }
        } else {
            if (!((net.minecraft.server.v1_14_R1.Chunk) from).getWorld().getWorldData().getName().equals(((net.minecraft.server.v1_14_R1.Chunk) to).getWorld().getWorldData().getName())) {
                return 100;
            }
        }
        double x;
        double z;
        if (MinecraftVersion.INSTANCE.getMajor() >= 9) {
            x = ((net.minecraft.server.v1_14_R1.Chunk) to).getPos().x - ((net.minecraft.server.v1_14_R1.Chunk) from).getPos().x;
            z = ((net.minecraft.server.v1_14_R1.Chunk) to).getPos().z - ((net.minecraft.server.v1_14_R1.Chunk) from).getPos().z;
        } else if (MinecraftVersion.INSTANCE.getMajor() >= 4) {
            x = ((net.minecraft.server.v1_12_R1.Chunk) to).locX - ((net.minecraft.server.v1_12_R1.Chunk) from).locX;
            z = ((net.minecraft.server.v1_12_R1.Chunk) to).locZ - ((net.minecraft.server.v1_12_R1.Chunk) from).locZ;
        } else {
            x = ((net.minecraft.server.v1_14_R1.Chunk) to).getPos().x - ((net.minecraft.server.v1_14_R1.Chunk) from).getPos().x;
            z = ((net.minecraft.server.v1_14_R1.Chunk) to).getPos().z - ((net.minecraft.server.v1_14_R1.Chunk) from).getPos().z;
        }
        return (int) Math.sqrt(x * x + z * z);
    }

    private void syncLight(Object lightEngine, Consumer<Object> task) {
        try {
            Object b;
            AtomicInteger c;
            if (MinecraftVersion.INSTANCE.getMajor() >= 9) {
                b = Reflex.Companion.getProperty(lightEngine, "taskMailbox", false, true, true);
                c = Reflex.Companion.getProperty(b, "status", false, true, true);
            } else {
                b = Reflex.Companion.getProperty(lightEngine, "b", false, true, true);
                c = Reflex.Companion.getProperty(b, "c", false, true, true);
            }
            int flags;
            long wait = -1L;
            while (!c.compareAndSet(flags = c.get() & ~2, flags | 2)) {
                if ((flags & 1) != 0) {
                    if (wait == -1) {
                        wait = System.currentTimeMillis() + 3 * 1000;
                        IOKt.info("ThreadedMailbox is closing. Will wait...");
                    } else if (System.currentTimeMillis() >= wait) {
                        IOKt.warning("Failed to enter critical section while ThreadedMailbox is closing");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            try {
                task.accept(lightEngine);
            } finally {
                while (!c.compareAndSet(flags = c.get(), flags & ~2)) {
                }
                Reflex.Companion.getProperty(b, "f", false, true, true);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void setRawLightLevelBlock(int level, Object position, Object lightEngineLayer) {
        if (level == 0) {
            ((LightEngineBlock) lightEngineLayer).a((BlockPosition) position);
        } else if (((LightEngineLayer) lightEngineLayer).a(SectionPosition.a((net.minecraft.server.v1_14_R1.BlockPosition) position)) != null) {
            try {
                ((LightEngineLayer) lightEngineLayer).a((net.minecraft.server.v1_14_R1.BlockPosition) position, level);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void setRawLightLevelSky(int level, Object position, Object lightEngineLayer) {
        if (level == 0) {
            ((LightEngineSky) lightEngineLayer).a((BlockPosition) position);
        } else if (((LightEngineLayer) lightEngineLayer).a(SectionPosition.a((net.minecraft.server.v1_14_R1.BlockPosition) position)) != null) {
            try {
                if (MinecraftVersion.INSTANCE.getMajor() >= 9) {
                    Object s = Reflex.Companion.getProperty(lightEngineLayer, "storage", false, true, true);
                    Reflex.Companion.invokeMethod(s, "e", new Object[0], false, true, true);
                } else {
                    Object s = Reflex.Companion.getProperty(lightEngineLayer, "c", false, true, true);
                    if (MinecraftVersion.INSTANCE.getMajor() >= 7) {
                        Reflex.Companion.invokeMethod(s, "d", new Object[0], false, true, true);
                    } else {
                        Reflex.Companion.invokeMethod(s, "c", new Object[0], false, true, true);
                    }
                }
                Object[] params = new Object[]{9223372036854775807L, ((net.minecraft.server.v1_14_R1.BlockPosition) position).asLong(), 15 - level, true};
                Reflex.Companion.invokeMethod(lightEngineLayer, "a", params, false, true, true);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
