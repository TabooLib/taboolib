package me.skymc.taboolib.other;

import java.text.DecimalFormat;
import java.util.Random;

import me.skymc.taboolib.methods.MethodsUtils;

public class NumberUtils {
	
	private static Random rand = new Random();
	private static DecimalFormat doubleFormat = new DecimalFormat("#.##");
	
	public static Random getRand() {
		return rand;
	}
	
	public static Double format(Double num) {
		return Double.valueOf(doubleFormat.format(num));
	}
    
	@Deprecated
    public static int getRandom() {
        return rand.nextInt(100);
    }
	
	@Deprecated
    public static boolean getChance(int a) {
        return getRandom() <= a ? true : false;
    }
    
    public static int getRandomInteger(Number l, Number u) {
    	Integer ll = Math.min(l.intValue(), u.intValue());
    	Integer uu = Math.max(l.intValue(), u.intValue());
		return rand.nextInt(uu) % (uu - ll + 1) + ll;
    }
    
    public static double getRandomDouble(Number l, Number u) {
    	double ll = Math.min(l.doubleValue(), u.doubleValue());
		double uu = Math.max(l.doubleValue(), u.doubleValue());
		double d = ll + rand.nextDouble() * (uu - ll);
		return Double.valueOf(doubleFormat.format(d));
    }
    
    public static int getInteger(String s) {
    	try {
    		return Integer.valueOf(s);
    	}
    	catch (Exception e) {
    		return 0;
    	}
    }
    
    public static double getDouble(String s) {
    	try {
    		return Double.valueOf(s);
    	}
    	catch (Exception e) {
    		return 0;
    	}
    }
    
    public static Boolean getBoolean(String s) {
    	try {
    		return Boolean.valueOf(s);
    	}
    	catch (Exception e) {
    		return false;
    	}
    }
}
