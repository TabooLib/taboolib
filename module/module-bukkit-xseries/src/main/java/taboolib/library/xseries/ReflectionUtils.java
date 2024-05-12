package taboolib.library.xseries;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TabooLib
 * taboolib.library.xseries.ReflectionUtils
 *
 * @author xseries, 坏黑
 * @since 2022/12/12 19:22
 */
public class ReflectionUtils {

    /**
     * Mojang remapped their NMS in 1.17: <a href="https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317">Spigot Thread</a>
     */
    // FIXME 此方式将在 Paper 1.20.5 中失效
    public static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    public static final int MAJOR_NUMBER;

    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @see #supports(int)
     * @since 4.0.0
     */
    public static final int MINOR_NUMBER;

    /**
     * The raw patch version number. Refers to the <a href="https://en.wikipedia.org/wiki/Software_versioning">major.minor.patch version scheme</a>.
     * E.g.
     * <ul>
     *     <li>{@code v1.20.4} to {@code 4}</li>
     *     <li>{@code v1.18.2} to {@code 2}</li>
     *     <li>{@code v1.19.1} to {@code 1}</li>
     * </ul>
     * <p>
     * I'd not recommend developers to support individual patches at all. You should always support the latest patch.
     * For example, between v1.14.0, v1.14.1, v1.14.2, v1.14.3 and v1.14.4 you should only support v1.14.4
     * <p>
     * This can be used to warn server owners when your plugin will break on older patches.
     *
     * @see #supportsPatch(int)
     * @since 7.0.0
     */
    public static final int PATCH_NUMBER;

    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     * <p>
     * <a href="https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-legacy/">Versions Legacy</a>
     * <p>
     * This will no longer work because of
     * <a href="https://forums.papermc.io/threads/paper-velocity-1-20-4.998/#post-2955">Paper no-relocation</a>
     * strategy.
     */
    @Nullable
    public static final String NMS_VERSION = findNMSVersionString();

    @Nullable
    public static String findNMSVersionString() {
        // This needs to be right below VERSION because of initialization order.
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

        return found;
    }


    static {
        /* Old way of doing this.
        String[] split = NMS_VERSION.substring(1).split("_");
        if (split.length < 1) {
            throw new IllegalStateException("Version number division error: " + Arrays.toString(split) + ' ' + getVersionInformation());
        }

        String minorVer = split[1];
        try {
            MINOR_NUMBER = Integer.parseInt(minorVer);
            if (MINOR_NUMBER < 0)
                throw new IllegalStateException("Negative minor number? " + minorVer + ' ' + getVersionInformation());
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to parse minor number: " + minorVer + ' ' + getVersionInformation(), ex);
        }
         */

        // NMS_VERSION               = v1_20_R3
        // Bukkit.getBukkitVersion() = 1.20.4-R0.1-SNAPSHOT
        // Bukkit.getVersion()       = git-Paper-364 (MC: 1.20.4)
        Matcher bukkitVer = Pattern
                // <patch> is optional for first releases like "1.8-R0.1-SNAPSHOT"
                .compile("^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)?")
                .matcher(Bukkit.getBukkitVersion());
        if (bukkitVer.find()) { // matches() won't work, we just want to match the start using "^"
            try {
                // group(0) gives the whole matched string, we just want the captured group.
                String patch = bukkitVer.group("patch");
                MAJOR_NUMBER = Integer.parseInt(bukkitVer.group("major"));
                MINOR_NUMBER = Integer.parseInt(bukkitVer.group("minor"));
                PATCH_NUMBER = Integer.parseInt((patch == null || patch.isEmpty()) ? "0" : patch);
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to parse minor number: " + bukkitVer + ' ' + getVersionInformation(), ex);
            }
        } else {
            throw new IllegalStateException("Cannot parse server version: \"" + Bukkit.getBukkitVersion() + '"');
        }
    }

    /**
     * Gets the full version information of the server. Useful for including in errors.
     *
     * @since 7.0.0
     */
    public static String getVersionInformation() {
        // Bukkit.getServer().getMinecraftVersion() is for Paper
        return "(NMS: " + NMS_VERSION + " | " +
                "Parsed: " + MAJOR_NUMBER + '.' + MINOR_NUMBER + '.' + PATCH_NUMBER + " | " +
                "Minecraft: " + Bukkit.getVersion() + " | " +
                "Bukkit: " + Bukkit.getBukkitVersion() + ')';
    }

    private ReflectionUtils() {
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @since 4.0.0
     */
    public static boolean supports(int minorNumber) {
        return MINOR_NUMBER >= minorNumber;
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the minor version to compare the server version with.
     * @param patchNumber the patch number to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @see #PATCH_NUMBER
     * @since 7.1.0
     */
    public static boolean supports(int minorNumber, int patchNumber) {
        return MINOR_NUMBER == minorNumber ? PATCH_NUMBER >= patchNumber : supports(minorNumber);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param patchNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #PATCH_NUMBER
     * @since 7.0.0
     * @deprecated use {@link #supports(int, int)}
     */
    @Deprecated
    public static boolean supportsPatch(int patchNumber) {
        return PATCH_NUMBER >= patchNumber;
    }

    /**
     * Get a CraftBukkit (org.bukkit.craftbukkit) class.
     *
     * @param name the name of the class to load.
     * @return the CraftBukkit class or null if not found.
     * @throws RuntimeException if the class could not be found.
     * @since 1.0.0
     */
    // FIXME 此方式将在 Paper 1.20.5 中失效
    @NotNull
    public static Class<?> getCraftClass(@NotNull String name) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + '.' + name);
    }
}