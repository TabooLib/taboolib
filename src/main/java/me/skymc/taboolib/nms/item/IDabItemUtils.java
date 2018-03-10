package me.skymc.taboolib.nms.item;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import me.skymc.taboolib.json.JSONArray;
import me.skymc.taboolib.json.JSONObject;

public interface IDabItemUtils{
	
	//@formatter:off
	public boolean hasBanner();
	public Object getNMSCopy(ItemStack is) throws Exception;
	public boolean hasTag(Object is) throws Exception;
	public ItemStack asCraftMirror(Object nis) throws Exception;
	public ItemStack asBukkitCopy(Object nmis) throws Exception;
	public String getName(ItemStack is);
	public Object getItem(Object nis) throws Exception;
	public Method getA();
	public String getRawName(ItemStack is);
	public String getItemName(ItemStack is);
	public Object getRegistry();
	public String getMinecraftName(ItemStack is);
	public Object getTag(Object is) throws Exception;
	public void setTag(Object is, Object tag1) throws Exception;
	public boolean isEmpty(Object tag) throws Exception;
	public Object getMap(Object tag) throws Exception;
	public void set(Object tag, String key, Object value) throws Exception;
	public void setString(Object tag, String key, String value) throws Exception;
	public void setShort(Object tag, String key, short value) throws Exception;
	public void setInt(Object tag, String key, int i) throws Exception;
	public void setDouble(Object tag, String key, double d) throws Exception;
	public void setLong(Object tag, String key, long l) throws Exception;
	public boolean hasKey(Object tag, String key) throws Exception;
	public Object get(Object tag, String key) throws Exception;
	public String getString(Object tag, String key) throws Exception;
	public int getInt(Object tag, String key) throws Exception;
	public double getDouble(Object tag, String key) throws Exception;
	public long getLong(Object tag, String key) throws Exception;
	public short getShort(Object tag, String key) throws Exception;
	public Object getNewNBTTagCompound() throws Exception;
	public boolean hasAttributeModifiersKey(Object tag) throws Exception;
	public Object getList(Object tag) throws Exception;
	public Object getList(Object tag, String name, int id) throws Exception;
	public boolean getUnbreakable(Object tag) throws Exception;
	public void setUnbreakable(Object tag, boolean value) throws Exception;
	public Object getNewNBTTagList() throws Exception;
	public void addToList(Object taglist, Object nbt) throws Exception;
	public int getSize(Object list) throws Exception;
	public Object get(Object tlist, int i) throws Exception;
	public Object getNewNBTTagByte(byte value) throws Exception;
	public Object getNewNBTTagByteArray(byte[] value) throws Exception;
	public Object getData(Object nbt) throws Exception;
	public Object createData(Object value) throws Exception;
	public Map<String, Object> convertCompoundTagToValueMap(Object nbt) throws Exception;
	public List<Object> convertListTagToValueList(Object nbttl) throws Exception;
	public Object convertValueMapToCompoundTag(Map<String, Object> map) throws Exception;
	public Object convertValueListToListTag(List<Object> list) throws Exception;
	@Deprecated
	public void convertListTagToJSON(Object nbttl, JSONArray ja, JSONArray helper) throws Exception;
	public void convertListTagToJSON(Object nbttl, JSONArray ja) throws Exception;
	@Deprecated
	public void convertCompoundTagToJSON(Object nbt, JSONObject jo, JSONObject helper) throws Exception;
	public void convertCompoundTagToJSON(Object nbt, JSONObject jo) throws Exception;
	@Deprecated
	public Object convertJSONToCompoundTag(JSONObject jo, JSONObject helper) throws Exception;
	public Object convertJSONToCompoundTag(JSONObject jo) throws Exception;
	@Deprecated
	public Object convertJSONToListTag(JSONArray ja, JSONArray helper) throws Exception;
	public Object convertJSONToListTag(JSONArray ja) throws Exception;
	@Deprecated
	public Object getDataJSON(String key, Object nbt, JSONObject jo, JSONObject helper) throws Exception;
	public JSONArray getDataJSON(Object nbt) throws Exception;
	@Deprecated
	public Object getDataJSON(Object nbt, JSONArray ja, JSONArray helper) throws Exception;
	@Deprecated
	public Object createDataJSON(String key, JSONObject jo, JSONObject helper) throws Exception;
	public Object createDataJSON(String key, JSONObject jo) throws Exception;
	public byte getByte(Object o);
	public short getShort(Object o);
	public int getInt(Object o);
	public double getDouble(Object o);
	public float getFloat(Object o);
	public long getLong(Object o);
	@Deprecated
	public Object createDataJSON(int key, JSONArray jo, JSONArray helper) throws Exception;
	public Object createDataJSON(int key, JSONArray jo) throws Exception;
	public boolean compareBaseTag(Object tag, Object tag1) throws Exception;
	public boolean compareCompoundTag(Object tag, Object tag1) throws Exception;
	public boolean compareListTag(Object tag, Object tag1) throws Exception;
	public boolean compare(ItemStack is1, ItemStack is2);
	public boolean canMerge(ItemStack add, ItemStack to);
	public boolean isModified(ItemStack is);
	public void sortByMaterial(List<ItemStack> items);
	public void sortByName(List<ItemStack> items);
	public void sortByAmount(List<ItemStack> items);
	public ItemStack convertJSONToItemStack(JSONObject jo) throws Exception;
	public JSONObject convertItemStackToJSON(ItemStack is) throws Exception;
}
