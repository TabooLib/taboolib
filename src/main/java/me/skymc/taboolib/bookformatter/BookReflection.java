package me.skymc.taboolib.bookformatter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The NMS helper for all the Book-API
 */
@SuppressWarnings({"ALL", "AliControlFlowStatementWithoutBraces"})
public final class BookReflection {

    private static final String version;
    private static final boolean doubleHands;

    private static final Class<?> craftMetaBookClass;
    private static final Field craftMetaBookField;
    private static final Method chatSerializerA;

    private static final Method craftPlayerGetHandle;

    //This method takes an enum that represents the player's hand only in versions >= 1.9
    //In the other versions it only takes the nms item
    private static final Method entityPlayerOpenBook;

    //only version >= 1.9
    private static final Object[] hands;

    //Older versions
    /*private static final Field entityHumanPlayerConnection;
    private static final Method playerConnectionSendPacket;

    private static final Constructor<?> packetPlayOutCustomPayloadConstructor;
    private static final Constructor<?> packetDataSerializerConstructor;*/

    private static final Method nmsItemStackSave;
    private static final Constructor<?> nbtTagCompoundConstructor;

    private static final Method craftItemStackAsNMSCopy;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        final int major, minor;
        Pattern pattern = Pattern.compile("v([0-9]+)_([0-9]+)");
        Matcher m = pattern.matcher(version);
        if (m.find()) {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
        } else {
            throw new IllegalStateException("Cannot parse version \"" + version + "\", make sure it follows \"v<major>_<minor>...\"");
        }
        doubleHands = major <= 1 && minor >= 9;
        try {
            craftMetaBookClass = getCraftClass("inventory.CraftMetaBook");
            craftMetaBookField = craftMetaBookClass.getDeclaredField("pages");
            craftMetaBookField.setAccessible(true);
            Class<?> chatSerializer = getNmsClass("IChatBaseComponent$ChatSerializer", false);
            //noinspection AliControlFlowStatementWithoutBraces
            if (chatSerializer == null) {
                chatSerializer = getNmsClass("ChatSerializer");
            }

            chatSerializerA = chatSerializer.getDeclaredMethod("a", String.class);

            final Class<?> craftPlayerClass = getCraftClass("entity.CraftPlayer");
            craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");

            final Class<?> entityPlayerClass = getNmsClass("EntityPlayer");
            final Class<?> itemStackClass = getNmsClass("ItemStack");
            if (doubleHands) {
                final Class<?> enumHandClass = getNmsClass("EnumHand");
                entityPlayerOpenBook = entityPlayerClass.getMethod("a", itemStackClass, enumHandClass);
                hands = enumHandClass.getEnumConstants();
            } else {
                entityPlayerOpenBook = entityPlayerClass.getMethod("openBook", itemStackClass);
                hands = null;
            }
            //Older versions
            /*entityHumanPlayerConnection = entityPlayerClass.getField("playerConnection");
            final Class<?> playerConnectionClass = getNmsClass("PlayerConnection");
            playerConnectionSendPacket = playerConnectionClass.getMethod("sendPacket", getNmsClass("Packet"));

            final Class<?> packetDataSerializerClasss = getNmsClass("PacketDataSerializer");
            packetPlayOutCustomPayloadConstructor = getNmsClass("PacketPlayOutCustomPayload").getConstructor(String.class, packetDataSerializerClasss);
            packetDataSerializerConstructor = packetDataSerializerClasss.getConstructor(ByteBuf.class);*/

            final Class<?> craftItemStackClass = getCraftClass("inventory.CraftItemStack");
            craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClazz = getNmsClass("ItemStack");
            Class<?> nbtTagCompoundClazz = getNmsClass("NBTTagCompound");
            nmsItemStackSave = nmsItemStackClazz.getMethod("save", nbtTagCompoundClazz);
            nbtTagCompoundConstructor = nbtTagCompoundClazz.getConstructor();

        } catch (Exception e) {
            throw new IllegalStateException("Cannot initiate reflections for " + version, e);
        }
    }


    /**
     * Sets the pages of the book to the components json equivalent
     *
     * @param meta       the book meta to change
     * @param components the pages of the book
     */
    @SuppressWarnings("unchecked")//reflections = unchecked warnings
    public static void setPages(BookMeta meta, BaseComponent[][] components) {
        try {
            List<Object> pages = (List<Object>) craftMetaBookField.get(meta);
            pages.clear();
            for (BaseComponent[] c : components) {
                final String json = ComponentSerializer.toString(c);
                //System.out.println("page:" + json); //Debug
                pages.add(chatSerializerA.invoke(null, json));
            }
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    /**
     * Append the pages of the book to the components json equivalent
     *
     * @param meta       the book meta to change
     * @param components the pages of the book
     */
    @SuppressWarnings("unchecked")//reflections = unchecked warnings
    public static void addPages(BookMeta meta, BaseComponent[][] components) {
        try {
            List<Object> pages = (List<Object>) craftMetaBookField.get(meta);
            for (BaseComponent[] c : components) {
                final String json = ComponentSerializer.toString(c);
                //System.out.println("page:" + json); //Debug
                pages.add(chatSerializerA.invoke(null, json));
            }
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    /**
     * Opens the book to a player (the player needs to have the book in one of his hands)
     *
     * @param player  the player
     * @param book    the book to open
     * @param offHand false if the book is in the right hand, true otherwise
     */
    public static void openBook(Player player, ItemStack book, boolean offHand) {
        //nms(player).openBook(nms(player), nms(book), hand);
        try {
            //Older versions:
            /*playerConnectionSendPacket.invoke(
                    entityHumanPlayerConnection.get(toNms(player)),
                    createBookOpenPacket()
            );*/
            if (doubleHands) {
                entityPlayerOpenBook.invoke(
                        toNms(player),
                        nmsCopy(book),
                        hands[offHand ? 1 : 0]
                );
            } else {
                entityPlayerOpenBook.invoke(
                        toNms(player),
                        nmsCopy(book)
                );
            }
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    //Older versions
    /*public static Object createBookOpenPacket() {
        //new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.buffer())));
        try {
            return packetPlayOutCustomPayloadConstructor.newInstance(
                    "MC|BOpen",
                    packetDataSerializerConstructor.newInstance(Unpooled.buffer())
            );
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }*/

    /**
     * Translates an ItemStack to his Chat-Component equivalent
     *
     * @param item the item to be converted
     * @return a Chat-Component equivalent of the parameter
     */
    public static BaseComponent[] itemToComponents(ItemStack item) {
        return jsonToComponents(itemToJson(item));
    }

    /**
     * Translates a json string to his Chat-Component equivalent
     *
     * @param json the json string to be converted
     * @return a Chat-Component equivalent of the parameter
     */
    public static BaseComponent[] jsonToComponents(String json) {
        return new BaseComponent[]{new TextComponent(json)};
    }

    /**
     * Translates an ItemStack to his json equivalent
     *
     * @param item the item to be converted
     * @return a json equivalent of the parameter
     */
    private static String itemToJson(ItemStack item) {
        try {
            //net.minecraft.server.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            Object nmsItemStack = nmsCopy(item);

            //net.minecraft.server.NBTTagCompound compound = new NBTTagCompound();
            //compound = nmsItemStack.save(compound);
            Object emptyTag = nbtTagCompoundConstructor.newInstance();
            Object json = nmsItemStackSave.invoke(nmsItemStack, emptyTag);
            return json.toString();
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    /**
     * An error thrown when this NMS-helper class doesn't support the running MC version
     */
    public static class UnsupportedVersionException extends RuntimeException {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 6835583513394319946L;

        /**
         * The current running version
         */
        private final String version = BookReflection.version;

        public String getVersion() {
            return version;
        }

        public UnsupportedVersionException(Exception e) {
            super("Error while executing reflections, submit to developers the following log (version: " + BookReflection.version + ")", e);
        }
    }


    /**
     * Gets the EntityPlayer handled by the argument
     *
     * @param player the Player handler
     * @return the handled class
     * @throws InvocationTargetException when some problems are found with the reflection
     * @throws IllegalAccessException    when some problems are found with the reflection
     */
    public static Object toNms(Player player) throws InvocationTargetException, IllegalAccessException {
        return craftPlayerGetHandle.invoke(player);
    }

    /**
     * Creates a NMS copy of the parameter
     *
     * @param item the ItemStack to be nms-copied
     * @return a NMS-ItemStack that is the equivalent of the one passed as argument
     * @throws InvocationTargetException when some problems are found with the reflection
     * @throws IllegalAccessException    when some problems are found with the reflection
     */
    public static Object nmsCopy(ItemStack item) throws InvocationTargetException, IllegalAccessException {
        return craftItemStackAsNMSCopy.invoke(null, item);
    }

    @SuppressWarnings("AliControlFlowStatementWithoutBraces")
    public static Class<?> getNmsClass(String className, boolean log) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ClassNotFoundException e) {
            //noinspection AliControlFlowStatementWithoutBraces
            if (log) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Class<?> getNmsClass(String className) {
        return getNmsClass(className, true);
    }


    private static Class<?> getCraftClass(String path) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
