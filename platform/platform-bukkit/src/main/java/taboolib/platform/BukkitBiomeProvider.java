package taboolib.platform;

import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.Nullable;

/**
 * TabooLib
 * taboolib.platform.BukkitWorldGenerator
 *
 * @author 坏黑
 * @since 2024/1/26 16:23
 */
public interface BukkitBiomeProvider {

    @Nullable
    BiomeProvider getDefaultBiomeProvider(String worldName, @Nullable String name);
}
