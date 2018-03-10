package me.skymc.taboolib.jsonformatter.hover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.json.JSONObject;
import me.skymc.taboolib.nms.item.DabItemUtils;
import me.skymc.taboolib.nms.item.IDabItemUtils;

public class ShowItemEvent extends HoverEvent{
	
	private JSONObject object = new JSONObject();
	
	public Object getItemTag(ItemStack item) {
		try {
			return DabItemUtils.getInstance().getTag(DabItemUtils.getInstance().getNMSCopy(item));
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public ShowItemEvent(ItemStack is){
		try{
			object.put("action", "show_item");
			StringBuilder tag = new StringBuilder();
			Object itemTag = getItemTag(is);
			if (itemTag != null) {
				tag.append(",tag:" + itemTag);
			}
			else {
				ItemMeta im = is.getItemMeta();
				List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<>();
				Map<Enchantment, Integer> enchants = is.getItemMeta().getEnchants();
				tag.append(",tag:{display:{Name:" + (enchants.size() > 0 ? "§b§o" : "§f") + ItemUtils.getCustomName(is));
				if (lore.size() > 0) {
					tag.append(",Lore:[");
					for (String s : lore){
						tag.append("\"" + s + "\",");
					}
					tag.delete(tag.length() - 1, tag.length());
					tag.append("]");
				}
				tag.append("}");
				if (enchants.size() > 0) {
					if(tag.length() > 6) {
						tag.append(",");
					}
					tag.append("ench:[");
					for (Entry<Enchantment, Integer> e : enchants.entrySet()) {
						tag.append("{id:" + e.getKey().getId() + ",lvl:" + e.getValue() + "},");
					}
					tag.delete(tag.length() - 1, tag.length());
					tag.append("]");
				}
				tag.append("}");
			}
			
			object.put("value", "{id:" + (TabooLib.getVerint() > 10700 ? DabItemUtils.getMinecraftName(is) : is.getTypeId()) + ",Count:" + is.getAmount() + tag.toString() + "}");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getEvent(){
		return object;
	}
	
}
