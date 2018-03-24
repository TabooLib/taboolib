package me.skymc.taboolib.nms.item;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.json.JSONArray;
import me.skymc.taboolib.json.JSONObject;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.nms.item.impl._164ItemUtils;
import me.skymc.taboolib.nms.item.impl._1710ItemUtils;
import me.skymc.taboolib.nms.item.impl._194ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class DabItemUtils {
	
	private static IDabItemUtils inst = load();
	
	private static IDabItemUtils load(){
		ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.KNOCKBACK, 20, true);
		is.setItemMeta(im);
		try{
			IDabItemUtils inst = new _194ItemUtils();
			inst.convertItemStackToJSON(is);
			MsgUtils.send("载入 1.9.4 Spigot 物品工具成功!");
			return inst;
        } catch (Exception ignored) {
		}
		try{
			_1710ItemUtils inst = new _1710ItemUtils();
			inst.convertItemStackToJSON(is);
			MsgUtils.send("载入 1.7.10 Cauldron 物品工具成功!");
			return inst;
        } catch (Exception ignored) {
		}
		try{
			IDabItemUtils inst = new _164ItemUtils();
			inst.convertItemStackToJSON(is);
			MsgUtils.send("载入 1.6.4 Cauldron 物品工具成功!");
			return inst;
        } catch (Exception ignored) {
		}
		MsgUtils.send("&4物品工具载入失败, 插件已关闭!");
		Bukkit.getPluginManager().disablePlugin(Main.getInst());
		return null;
	}
	
	public static IDabItemUtils getInstance(){
		return inst;
	}
	
	@Deprecated
	public static boolean hasBanner(){
		return inst.hasBanner();
	}
	
	@Deprecated
	public static Object getNMSCopy(ItemStack is) throws Exception{
		return inst.getNMSCopy(is);
	}
	
	@Deprecated
	public static boolean hasTag(Object is) throws Exception{
		return inst.hasTag(is);
	}
	
	@Deprecated
	public static ItemStack asCraftMirror(Object nis) throws Exception{
		return inst.asCraftMirror(nis);
	}
	
	@Deprecated
	public static ItemStack asBukkitCopy(Object nmis) throws Exception{
		return inst.asBukkitCopy(nmis);
	}
	
	@Deprecated
	public static String getName(ItemStack is){
		return inst.getName(is);
	}
	
	@Deprecated
	public static Object getItem(Object nis) throws Exception{
		return inst.getItem(nis);
	}
	
	@Deprecated
	public static Method getA(){
		return inst.getA();
	}
	
	@Deprecated
	public static String getRawName(ItemStack is){
		return inst.getRawName(is);
	}
	
	@Deprecated
	public static String getItemName(ItemStack is){
		return inst.getItemName(is);
	}
	
	@Deprecated
	public static Object getRegistry(){
		return inst.getRegistry();
	}
	
	@Deprecated
	public static String getMinecraftName(ItemStack is){
		return inst.getMinecraftName(is);
	}
	
	@Deprecated
	public static Object getTag(Object is) throws Exception{
		return inst.getTag(is);
	}
	
	@Deprecated
	public static void setTag(Object is, Object tag1) throws Exception{
		inst.setTag(is, tag1);
	}
	
	@Deprecated
	public static boolean isEmpty(Object tag) throws Exception{
		return inst.isEmpty(tag);
	}
	
	@Deprecated
	public static Object getMap(Object tag) throws Exception{
		return inst.getMap(tag);
	}
	
	@Deprecated
	public static void set(Object tag, String key, Object value) throws Exception{
		inst.set(tag, key, value);
	}
	
	@Deprecated
	public static void setString(Object tag, String key, String value) throws Exception{
		inst.setString(tag, key, value);
	}
	
	@Deprecated
	public static void setShort(Object tag, String key, short value) throws Exception{
		inst.setShort(tag, key, value);
	}
	
	@Deprecated
	public static void setInt(Object tag, String key, int i) throws Exception{
		inst.setInt(tag, key, i);
	}
	
	@Deprecated
	public static void setDouble(Object tag, String key, double d) throws Exception{
		inst.setDouble(tag, key, d);
	}
	
	@Deprecated
	public static void setLong(Object tag, String key, long l) throws Exception{
		inst.setLong(tag, key, l);
	}
	
	@Deprecated
	public static boolean hasKey(Object tag, String key) throws Exception{
		return inst.hasKey(tag, key);
	}
	
	@Deprecated
	public static Object get(Object tag, String key) throws Exception{
		return inst.get(tag, key);
	}
	
	@Deprecated
	public static String getString(Object tag, String key) throws Exception{
		return inst.getString(tag, key);
	}
	
	@Deprecated
	public static int getInt(Object tag, String key) throws Exception{
		return inst.getInt(tag, key);
	}
	
	@Deprecated
	public static double getDouble(Object tag, String key) throws Exception{
		return inst.getDouble(tag, key);
	}
	
	@Deprecated
	public static long getLong(Object tag, String key) throws Exception{
		return inst.getLong(tag, key);
	}
	
	@Deprecated
	public static short getShort(Object tag, String key) throws Exception{
		return inst.getShort(tag, key);
	}
	
	@Deprecated
	public static Object getNewNBTTagCompound() throws Exception{
		return inst.getNewNBTTagCompound();
	}
	
	@Deprecated
	public static boolean hasAttributeModifiersKey(Object tag) throws Exception{
		return inst.hasAttributeModifiersKey(tag);
	}
	
	@Deprecated
	public static Object getList(Object tag) throws Exception{
		return inst.getList(tag);
	}
	
	@Deprecated
	public static Object getList(Object tag, String name, int id) throws Exception{
		return inst.getList(tag, name, id);
	}
	
	@Deprecated
	public static boolean getUnbreakable(Object tag) throws Exception{
		return inst.getUnbreakable(tag);
	}
	
	@Deprecated
	public static void setUnbreakable(Object tag, boolean value) throws Exception{
		inst.setUnbreakable(tag, value);
	}
	
	@Deprecated
	public static Object getNewNBTTagList() throws Exception{
		return inst.getNewNBTTagList();
	}
	
	@Deprecated
	public static void addToList(Object taglist, Object nbt) throws Exception{
		inst.addToList(taglist, nbt);
	}
	
	@Deprecated
	public static int getSize(Object list) throws Exception{
		return inst.getSize(list);
	}
	
	@Deprecated
	public static Object get(Object tlist, int i) throws Exception{
		return inst.get(tlist, i);
	}
	
	@Deprecated
	public static Object getNewNBTTagByte(byte value) throws Exception{
		return inst.getNewNBTTagByte(value);
	}
	
	@Deprecated
	public static Object getNewNBTTagByteArray(byte[] value) throws Exception{
		return inst.getNewNBTTagByteArray(value);
	}
	
	@Deprecated
	public static Object getData(Object nbt) throws Exception{
		return inst.getData(nbt);
	}
	
	@Deprecated
	public static Object createData(Object value) throws Exception{
		return inst.createData(value);
	}
	
	@Deprecated
	public static Map<String, Object> convertCompoundTagToValueMap(Object nbt) throws Exception{
		return inst.convertCompoundTagToValueMap(nbt);
	}
	
	@Deprecated
	public static List<Object> convertListTagToValueList(Object nbttl) throws Exception{
		return inst.convertListTagToValueList(nbttl);
	}
	
	@Deprecated
	public static Object convertValueMapToCompoundTag(Map<String, Object> map) throws Exception{
		return inst.convertValueMapToCompoundTag(map);
	}
	
	@Deprecated
	public static Object convertValueListToListTag(List<Object> list) throws Exception{
		return inst.convertValueListToListTag(list);
	}
	
	@Deprecated
	public static void convertListTagToJSON(Object nbttl, JSONArray ja, JSONArray helper) throws Exception{
		inst.convertListTagToJSON(nbttl, ja, helper);
	}
	
	@Deprecated
	public static void convertListTagToJSON(Object nbttl, JSONArray ja) throws Exception{
		inst.convertListTagToJSON(nbttl, ja);
	}
	
	@Deprecated
	public static void convertCompoundTagToJSON(Object nbt, JSONObject jo, JSONObject helper) throws Exception{
		inst.convertCompoundTagToJSON(nbt, jo, helper);
	}
	
	@Deprecated
	public static void convertCompoundTagToJSON(Object nbt, JSONObject jo) throws Exception{
		inst.convertCompoundTagToJSON(nbt, jo);
	}
	
	@Deprecated
	public static Object convertJSONToCompoundTag(JSONObject jo, JSONObject helper) throws Exception{
		return inst.convertJSONToCompoundTag(jo, helper);
	}
	
	@Deprecated
	public static Object convertJSONToCompoundTag(JSONObject jo) throws Exception{
		return inst.convertJSONToCompoundTag(jo);
	}
	
	@Deprecated
	public static Object convertJSONToListTag(JSONArray ja, JSONArray helper) throws Exception{
		return inst.convertJSONToListTag(ja, helper);
	}
	
	@Deprecated
	public static Object convertJSONToListTag(JSONArray ja) throws Exception{
		return inst.convertJSONToListTag(ja);
	}
	
	@Deprecated
	public static Object getDataJSON(String key, Object nbt, JSONObject jo, JSONObject helper) throws Exception{
		return inst.getDataJSON(key, nbt, jo, helper);
	}
	
	@Deprecated
	public static JSONArray getDataJSON(Object nbt) throws Exception{
		return inst.getDataJSON(nbt);
	}
	
	@Deprecated
	public static Object getDataJSON(Object nbt, JSONArray ja, JSONArray helper) throws Exception{
		return inst.getDataJSON(nbt, ja, helper);
	}
	
	@Deprecated
	public static Object createDataJSON(String key, JSONObject jo, JSONObject helper) throws Exception{
		return inst.createDataJSON(key, jo, helper);
	}
	
	@Deprecated
	public static Object createDataJSON(String key, JSONObject jo) throws Exception{
		return inst.createDataJSON(key, jo);
	}
	
	@Deprecated
	public static byte getByte(Object o){
		return inst.getByte(o);
	}
	
	@Deprecated
	public static short getShort(Object o){
		return inst.getShort(o);
	}
	
	@Deprecated
	public static int getInt(Object o){
		return inst.getInt(o);
	}
	
	@Deprecated
	public static double getDouble(Object o){
		return inst.getDouble(o);
	}
	
	@Deprecated
	public static float getFloat(Object o){
		return inst.getFloat(o);
	}
	
	@Deprecated
	public static long getLong(Object o){
		return inst.getLong(o);
	}
	
	@Deprecated
	public static Object createDataJSON(int key, JSONArray jo, JSONArray helper) throws Exception{
		return inst.createDataJSON(key, jo, helper);
	}
	
	@Deprecated
	public static Object createDataJSON(int key, JSONArray jo) throws Exception{
		return inst.createDataJSON(key, jo);
	}
	
	@Deprecated
	public static boolean compareBaseTag(Object tag, Object tag1) throws Exception{
		return inst.compareBaseTag(tag, tag1);
	}
	
	@Deprecated
	public static boolean compareCompoundTag(Object tag, Object tag1) throws Exception{
		return inst.compareCompoundTag(tag, tag1);
	}
	
	@Deprecated
	public static boolean compareListTag(Object tag, Object tag1) throws Exception{
		return inst.compareListTag(tag, tag1);
	}
	
	@Deprecated
	public static boolean compare(ItemStack is1, ItemStack is2){
		return inst.compare(is1, is2);
	}
	
	@Deprecated
	public static boolean canMerge(ItemStack add, ItemStack to){
		return compare(add, to);
	}
	
	@Deprecated
	public static boolean isModified(ItemStack is){
		return inst.isModified(is);
	}
	
	@Deprecated
	public static void sortByMaterial(List<ItemStack> items){
		inst.sortByMaterial(items);
	}
	
	@Deprecated
	public static void sortByName(List<ItemStack> items){
		inst.sortByName(items);
	}
	
	@Deprecated
	public static void sortByAmount(List<ItemStack> items){
		inst.sortByAmount(items);
	}
	
	@Deprecated
	public static ItemStack convertJSONToItemStack(JSONObject jo) throws Exception{
		return inst.convertJSONToItemStack(jo);
	}
	
	@Deprecated
	public static JSONObject convertItemStackToJSON(ItemStack is) throws Exception{
		return inst.convertItemStackToJSON(is);
	}
}
