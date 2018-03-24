package me.skymc.taboolib.nms.item;

import me.skymc.taboolib.json.JSONArray;
import me.skymc.taboolib.json.JSONObject;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface IDabItemUtils{
	
	//@formatter:off
    boolean hasBanner();

    Object getNMSCopy(ItemStack is) throws Exception;

    boolean hasTag(Object is) throws Exception;

    ItemStack asCraftMirror(Object nis) throws Exception;

    ItemStack asBukkitCopy(Object nmis) throws Exception;

    String getName(ItemStack is);

    Object getItem(Object nis) throws Exception;

    Method getA();

    String getRawName(ItemStack is);

    String getItemName(ItemStack is);

    Object getRegistry();

    String getMinecraftName(ItemStack is);

    Object getTag(Object is) throws Exception;

    void setTag(Object is, Object tag1) throws Exception;

    boolean isEmpty(Object tag) throws Exception;

    Object getMap(Object tag) throws Exception;

    void set(Object tag, String key, Object value) throws Exception;

    void setString(Object tag, String key, String value) throws Exception;

    void setShort(Object tag, String key, short value) throws Exception;

    void setInt(Object tag, String key, int i) throws Exception;

    void setDouble(Object tag, String key, double d) throws Exception;

    void setLong(Object tag, String key, long l) throws Exception;

    boolean hasKey(Object tag, String key) throws Exception;

    Object get(Object tag, String key) throws Exception;

    String getString(Object tag, String key) throws Exception;

    int getInt(Object tag, String key) throws Exception;

    double getDouble(Object tag, String key) throws Exception;

    long getLong(Object tag, String key) throws Exception;

    short getShort(Object tag, String key) throws Exception;

    Object getNewNBTTagCompound() throws Exception;

    boolean hasAttributeModifiersKey(Object tag) throws Exception;

    Object getList(Object tag) throws Exception;

    Object getList(Object tag, String name, int id) throws Exception;

    boolean getUnbreakable(Object tag) throws Exception;

    void setUnbreakable(Object tag, boolean value) throws Exception;

    Object getNewNBTTagList() throws Exception;

    void addToList(Object taglist, Object nbt) throws Exception;

    int getSize(Object list) throws Exception;

    Object get(Object tlist, int i) throws Exception;

    Object getNewNBTTagByte(byte value) throws Exception;

    Object getNewNBTTagByteArray(byte[] value) throws Exception;

    Object getData(Object nbt) throws Exception;

    Object createData(Object value) throws Exception;

    Map<String, Object> convertCompoundTagToValueMap(Object nbt) throws Exception;

    List<Object> convertListTagToValueList(Object nbttl) throws Exception;

    Object convertValueMapToCompoundTag(Map<String, Object> map) throws Exception;

    Object convertValueListToListTag(List<Object> list) throws Exception;
	@Deprecated
    void convertListTagToJSON(Object nbttl, JSONArray ja, JSONArray helper) throws Exception;

    void convertListTagToJSON(Object nbttl, JSONArray ja) throws Exception;
	@Deprecated
    void convertCompoundTagToJSON(Object nbt, JSONObject jo, JSONObject helper) throws Exception;

    void convertCompoundTagToJSON(Object nbt, JSONObject jo) throws Exception;
	@Deprecated
    Object convertJSONToCompoundTag(JSONObject jo, JSONObject helper) throws Exception;

    Object convertJSONToCompoundTag(JSONObject jo) throws Exception;
	@Deprecated
    Object convertJSONToListTag(JSONArray ja, JSONArray helper) throws Exception;

    Object convertJSONToListTag(JSONArray ja) throws Exception;
	@Deprecated
    Object getDataJSON(String key, Object nbt, JSONObject jo, JSONObject helper) throws Exception;

    JSONArray getDataJSON(Object nbt) throws Exception;
	@Deprecated
    Object getDataJSON(Object nbt, JSONArray ja, JSONArray helper) throws Exception;
	@Deprecated
    Object createDataJSON(String key, JSONObject jo, JSONObject helper) throws Exception;

    Object createDataJSON(String key, JSONObject jo) throws Exception;

    byte getByte(Object o);

    short getShort(Object o);

    int getInt(Object o);

    double getDouble(Object o);

    float getFloat(Object o);

    long getLong(Object o);
	@Deprecated
    Object createDataJSON(int key, JSONArray jo, JSONArray helper) throws Exception;

    Object createDataJSON(int key, JSONArray jo) throws Exception;

    boolean compareBaseTag(Object tag, Object tag1) throws Exception;

    boolean compareCompoundTag(Object tag, Object tag1) throws Exception;

    boolean compareListTag(Object tag, Object tag1) throws Exception;

    boolean compare(ItemStack is1, ItemStack is2);

    boolean canMerge(ItemStack add, ItemStack to);

    boolean isModified(ItemStack is);

    void sortByMaterial(List<ItemStack> items);

    void sortByName(List<ItemStack> items);

    void sortByAmount(List<ItemStack> items);

    ItemStack convertJSONToItemStack(JSONObject jo) throws Exception;

    JSONObject convertItemStackToJSON(ItemStack is) throws Exception;
}
