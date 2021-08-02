package taboolib.module.map

import org.bukkit.Bukkit

enum class Version(val versionInt: Int) {

    v1_7(10700),
    v1_8(10800),
    v1_9(10900),
    v1_10(11000),
    v1_11(11100),
    v1_12(11200),
    v1_13(11300),
    v1_14(11400),
    v1_15(11500),
    v1_16_R3(11604),
    v1_16(11600),
    v1_17(11700),
    vNull(0);

    companion object {

        @JvmStatic
        val bukkitVersion: String
            get() = Bukkit.getServer().javaClass.name.split(".").toTypedArray()[3]

        @JvmStatic
        fun getNmsClass(name: String): Class<*>? {
            return try {
                Class.forName("net.minecraft.server.$bukkitVersion.$name")
            } catch (e: Exception) {
                return null;
            }
        }

        @JvmStatic
        fun getClass(name: String): Class<*>? {
            return try {
                Class.forName(name)
            } catch (e: Exception) {
                return null;
            }
        }

        @JvmStatic
        fun getObcClass(name: String): Class<*> {
            return Class.forName("org.bukkit.craftbukkit.$bukkitVersion.$name")
        }

        @JvmStatic
        fun isAfter(`in`: Version): Boolean {
            return getCurrentVersion().versionInt >= `in`.versionInt
        }

        @JvmStatic
        fun isBefore(`in`: Version): Boolean {
            return getCurrentVersion().versionInt < `in`.versionInt
        }

        @JvmStatic
        fun getCurrentVersion(): Version {
            for (value in values()) {
                if (bukkitVersion.startsWith(value.name)) {
                    return value
                }
            }
            return vNull
        }
    }

}