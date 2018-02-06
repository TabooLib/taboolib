package me.skymc.taboolib.enchantment;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentUtils {
	
	public static void setAcceptingNew(boolean value) {
		try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
}
