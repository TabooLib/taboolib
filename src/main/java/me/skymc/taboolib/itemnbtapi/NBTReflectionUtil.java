package me.skymc.taboolib.itemnbtapi;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.itemnbtapi.utils.GsonWrapper;
import me.skymc.taboolib.itemnbtapi.utils.MethodNames;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Stack;

// TODO: finish codestyle cleanup -sgdc3
public class NBTReflectionUtil {

    private static final String version = TabooLib.getVersion();
    
    @SuppressWarnings("rawtypes")
    private static Class getCraftItemStack() {

        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception ex) {
        	MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class getCraftEntity() {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
        } catch (Exception ex) {
        	MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getNBTBase() {
        try {
            return Class.forName("net.minecraft.server." + version + ".NBTBase");
        } catch (Exception ex) {
        	MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getNBTTagString() {
        try {
            return Class.forName("net.minecraft.server." + version + ".NBTTagString");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected static Class getNMSItemStack() {
        try {
            return Class.forName("net.minecraft.server." + version + ".ItemStack");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getNBTTagCompound() {
        try {
            return Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getNBTCompressedStreamTools() {
        try {
            return Class.forName("net.minecraft.server." + version + ".NBTCompressedStreamTools");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected static Class getMojangsonParser() {
        try {
            return Class.forName("net.minecraft.server." + version + ".MojangsonParser");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getTileEntity() {
        try {
            return Class.forName("net.minecraft.server." + version + ".TileEntity");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Class getCraftWorld() {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    public static Object getNewNBTTag() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            return c.newInstance();
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    private static Object getNewBlockPosition(int x, int y, int z) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            @SuppressWarnings("rawtypes")
            Class clazz = Class.forName("net.minecraft.server." + version + ".BlockPosition");
            return clazz.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
        } catch (Exception ex) {
            
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
            return null;
        }
    }

    public static Object setNBTTag(Object NBTTag, Object NMSItem) {
        try {
            Method method;
            method = NMSItem.getClass().getMethod("setTag", NBTTag.getClass());
            method.invoke(NMSItem, NBTTag);
            return NMSItem;
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object getNMSItemStack(ItemStack item) {
        @SuppressWarnings("rawtypes")
        Class clazz = getCraftItemStack();
        Method method;
        try {
            method = clazz.getMethod("asNMSCopy", ItemStack.class);
            return method.invoke(clazz, item);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object getNMSEntity(Entity entity) {
        @SuppressWarnings("rawtypes")
        Class clazz = getCraftEntity();
        Method method;
        try {
            method = clazz.getMethod("getHandle");
            return method.invoke(getCraftEntity().cast(entity));
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static Object parseNBT(String json) {
        @SuppressWarnings("rawtypes")
        Class cis = getMojangsonParser();
        java.lang.reflect.Method method;
        try {
            method = cis.getMethod("parse", String.class);
            return method.invoke(null, json);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }
    
    @SuppressWarnings({"unchecked"})
    public static Object readNBTFile(FileInputStream stream) {
        @SuppressWarnings("rawtypes")
        Class clazz = getNBTCompressedStreamTools();
        Method method;
        try {
            method = clazz.getMethod("a", InputStream.class);
            return method.invoke(clazz, stream);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static Object saveNBTFile(Object nbt, FileOutputStream stream) {
        @SuppressWarnings("rawtypes")
        Class clazz = getNBTCompressedStreamTools();
        Method method;
        try {
            method = clazz.getMethod("a", getNBTTagCompound(), OutputStream.class);
            return method.invoke(clazz, nbt, stream);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static ItemStack getBukkitItemStack(Object item) {
        @SuppressWarnings("rawtypes")
        Class clazz = getCraftItemStack();
        Method method;
        try {
            method = clazz.getMethod("asCraftMirror", item.getClass());
            Object answer = method.invoke(clazz, item);
            return (ItemStack) answer;
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static Object getItemRootNBTTagCompound(Object nmsitem) {
        @SuppressWarnings("rawtypes")
        Class clazz = nmsitem.getClass();
        Method method;
        try {
            method = clazz.getMethod("getTag");
            return method.invoke(nmsitem);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }
    
    @SuppressWarnings({"unchecked"})
    public static Object convertNBTCompoundtoNMSItem(NBTCompound nbtcompound) {
        @SuppressWarnings("rawtypes")
        Class clazz = getNMSItemStack();
        try {
            return clazz.getConstructor(getNBTTagCompound()).newInstance(nbtcompound.getCompound());
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }
    
    @SuppressWarnings({"unchecked"})
    public static NBTContainer convertNMSItemtoNBTCompound(Object nmsitem) {
        @SuppressWarnings("rawtypes")
        Class clazz = nmsitem.getClass();
        Method method;
        try {
            method = clazz.getMethod("save", getNBTTagCompound());
            Object answer = method.invoke(nmsitem, getNewNBTTag());
            return new NBTContainer(answer);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static Object getEntityNBTTagCompound(Object nmsitem) {
        @SuppressWarnings("rawtypes")
        Class c = nmsitem.getClass();
        Method method;
        try {
            method = c.getMethod(MethodNames.getEntityNbtGetterMethodName(), getNBTTagCompound());
            Object nbt = getNBTTagCompound().newInstance();
            Object answer = method.invoke(nmsitem, nbt);
            if (answer == null)
                answer = nbt;
            return answer;
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    public static Object setEntityNBTTag(Object NBTTag, Object NMSItem) {
        try {
            Method method;
            method = NMSItem.getClass().getMethod(MethodNames.getEntityNbtSetterMethodName(), getNBTTagCompound());
            method.invoke(NMSItem, NBTTag);
            return NMSItem;
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static Object getTileEntityNBTTagCompound(BlockState tile) {
        Method method;
        try {
            Object pos = getNewBlockPosition(tile.getX(), tile.getY(), tile.getZ());
            Object cworld = getCraftWorld().cast(tile.getWorld());
            Object nmsworld = cworld.getClass().getMethod("getHandle").invoke(cworld);
            Object o = nmsworld.getClass().getMethod("getTileEntity", pos.getClass()).invoke(nmsworld, pos);
            method = getTileEntity().getMethod(MethodNames.getTileDataMethodName(), getNBTTagCompound());
            Object tag = getNBTTagCompound().newInstance();
            Object answer = method.invoke(o, tag);
            if (answer == null)
                answer = tag;
            return answer;
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    public static void setTileEntityNBTTagCompound(BlockState tile, Object comp) {
        Method method;
        try {
            Object pos = getNewBlockPosition(tile.getX(), tile.getY(), tile.getZ());
            Object cworld = getCraftWorld().cast(tile.getWorld());
            Object nmsworld = cworld.getClass().getMethod("getHandle").invoke(cworld);
            Object o = nmsworld.getClass().getMethod("getTileEntity", pos.getClass()).invoke(nmsworld, pos);
            method = getTileEntity().getMethod("a", getNBTTagCompound());
            method.invoke(o, comp);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
    }


    @SuppressWarnings("unchecked")
    public static Object getSubNBTTagCompound(Object compound, String name) {
        @SuppressWarnings("rawtypes")
        Class c = compound.getClass();
        Method method;
        try {
            method = c.getMethod("getCompound", String.class);
            return method.invoke(compound, name);
        } catch (Exception e) {
             MsgUtils.warn("NBT 操作出现异常: §7" + e.getMessage());
        }
        return null;
    }

    public static void addNBTTagCompound(NBTCompound comp, String name) {
        if (name == null) {
            remove(comp, name);
            return;
        }
        Object nbttag = comp.getCompound();
        if (nbttag == null) {
            nbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(nbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("set", String.class, getNBTBase());
            method.invoke(workingtag, name, getNBTTagCompound().newInstance());
            comp.setCompound(nbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Boolean valideCompound(NBTCompound comp) {
        Object root = comp.getCompound();
        if (root == null) {
            root = getNewNBTTag();
        }
        return (gettoCompount(root, comp)) != null;
    }

    private static Object gettoCompount(Object nbttag, NBTCompound comp) {
        Stack<String> structure = new Stack<>();
        while (comp.getParent() != null) {
            structure.add(comp.getName());
            comp = comp.getParent();
        }
        while (!structure.isEmpty()) {
            nbttag = getSubNBTTagCompound(nbttag, structure.pop());
            if (nbttag == null) {
                return null;
            }
        }
        return nbttag;
    }
    
    public static void addOtherNBTCompound(NBTCompound comp, NBTCompound nbtcompound) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("a", getNBTTagCompound());
            method.invoke(workingtag, nbtcompound.getCompound());
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static void setString(NBTCompound comp, String key, String text) {
        if (text == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setString", String.class, String.class);
            method.invoke(workingtag, key, text);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static String getString(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getString", String.class);
            return (String) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static String getContent(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("get", String.class);
            return method.invoke(workingtag, key).toString();
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setInt(NBTCompound comp, String key, Integer i) {
        if (i == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setInt", String.class, int.class);
            method.invoke(workingtag, key, i);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Integer getInt(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getInt", String.class);
            return (Integer) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setByteArray(NBTCompound comp, String key, byte[] b) {
        if (b == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setByteArray", String.class, byte[].class);
            method.invoke(workingtag, key, b);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static byte[] getByteArray(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getByteArray", String.class);
            return (byte[]) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setIntArray(NBTCompound comp, String key, int[] i) {
        if (i == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setIntArray", String.class, int[].class);
            method.invoke(workingtag, key, i);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static int[] getIntArray(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getIntArray", String.class);
            return (int[]) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setFloat(NBTCompound comp, String key, Float f) {
        if (f == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setFloat", String.class, float.class);
            method.invoke(workingtag, key, f);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Float getFloat(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getFloat", String.class);
            return (Float) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setLong(NBTCompound comp, String key, Long f) {
        if (f == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setLong", String.class, long.class);
            method.invoke(workingtag, key, f);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Long getLong(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getLong", String.class);
            return (Long) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setShort(NBTCompound comp, String key, Short f) {
        if (f == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setShort", String.class, short.class);
            method.invoke(workingtag, key, f);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Short getShort(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getShort", String.class);
            return (Short) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setByte(NBTCompound comp, String key, Byte f) {
        if (f == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setByte", String.class, byte.class);
            method.invoke(workingtag, key, f);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Byte getByte(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getByte", String.class);
            return (Byte) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setDouble(NBTCompound comp, String key, Double d) {
        if (d == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setDouble", String.class, double.class);
            method.invoke(workingtag, key, d);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Double getDouble(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getDouble", String.class);
            return (Double) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static byte getType(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return 0;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod(MethodNames.getTypeMethodName(), String.class);
            return (byte) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return 0;
    }

    public static void setBoolean(NBTCompound comp, String key, Boolean d) {
        if (d == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("setBoolean", String.class, boolean.class);
            method.invoke(workingtag, key, d);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Boolean getBoolean(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getBoolean", String.class);
            return (Boolean) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void set(NBTCompound comp, String key, Object val) {
        if (val == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) {
            new Throwable("InvalideCompound").printStackTrace();
            return;
        }
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("set", String.class, getNBTBase());
            method.invoke(workingtag, key, val);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static NBTList getList(NBTCompound comp, String key, NBTType type) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("getList", String.class, int.class);
            return new NBTList(comp, key, type, method.invoke(workingtag, key, type.getId()));
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    public static void setObject(NBTCompound comp, String key, Object value) {
        try {
            String json = GsonWrapper.getString(value);
            setString(comp, key, json);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static <T> T getObject(NBTCompound comp, String key, Class<T> type) {
        String json = getString(comp, key);
        if (json == null) {
            return null;
        }
        return GsonWrapper.deserializeJson(json, type);
    }

    public static void remove(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("remove", String.class);
            method.invoke(workingtag, key);
            comp.setCompound(rootnbttag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
    }

    public static Boolean hasKey(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("hasKey", String.class);
            return (Boolean) method.invoke(workingtag, key);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getKeys(NBTCompound comp) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = getNewNBTTag();
        }
        if (!valideCompound(comp)) return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        Method method;
        try {
            method = workingtag.getClass().getMethod("c");
            return (Set<String>) method.invoke(workingtag);
        } catch (Exception ex) {
             MsgUtils.warn("NBT 操作出现异常: §7" + ex.getMessage());
        }
        return null;
    }

}
