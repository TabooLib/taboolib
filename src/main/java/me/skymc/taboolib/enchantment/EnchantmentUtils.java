package me.skymc.taboolib.enchantment;

import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

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
