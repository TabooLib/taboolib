package taboolib.platform;

import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BukkitWorldGenerator {

    @Nullable
    ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id);
}
