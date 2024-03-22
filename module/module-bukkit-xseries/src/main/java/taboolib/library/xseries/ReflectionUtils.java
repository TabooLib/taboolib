package taboolib.library.xseries;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.library.xseries.ReflectionUtils
 *
 * @author xseries, 坏黑
 * @since 2022/12/12 19:22
 */
public class ReflectionUtils {

    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     */
    public static final String VERSION;

    static { // This needs to be right below VERSION because of initialization order.
        // This package loop is used to avoid implementation-dependant strings like Bukkit.getVersion() or Bukkit.getBukkitVersion()
        // which allows easier testing as well.
        String found = null;
        for (Package pack : Package.getPackages()) {
            String name = pack.getName();

            // FIXME 此方式将在 Paper 1.20.5 中失效
            // .v because there are other packages.
            if (name.startsWith("org.bukkit.craftbukkit.v")) {
                found = pack.getName().split("\\.")[3];

                // Just a final guard to make sure it finds this important class.
                // As a protection for forge+bukkit implementation that tend to mix versions.
                // The real CraftPlayer should exist in the package.
                // Note: Doesn't seem to function properly. Will need to separate the version
                // handler for NMS and CraftBukkit for softwares like catmc.
                try {
                    Class.forName("org.bukkit.craftbukkit." + found + ".entity.CraftPlayer");
                    break;
                } catch (ClassNotFoundException e) {
                    found = null;
                }
            }
        }
        if (found == null) throw new IllegalArgumentException("Failed to parse server version. Could not find any package starting with name: 'org.bukkit.craftbukkit.v'");
        VERSION = found;
    }

    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @since 4.0.0
     */
    // FIXME 此方式将在 Paper 1.20.5 中失效
    public static final int VER = Integer.parseInt(VERSION.substring(1).split("_")[1]);

    // FIXME 此方式将在 Paper 1.20.5 中失效
    public static final String CRAFTBUKKIT = "org.bukkit.craftbukkit." + VERSION + '.';

    // FIXME 此方式将在 Paper 1.20.5 中失效
    public static Class<?> getCraftClass(@NotNull String name) {
        try {
            return Class.forName(CRAFTBUKKIT + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param version the version to compare the server version with.
     *
     * @return true if the version is equal or newer, otherwise false.
     * @since 4.0.0
     */
    public static boolean supports(int version) {return VER >= version;}
}
