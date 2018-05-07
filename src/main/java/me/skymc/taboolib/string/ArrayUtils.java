package me.skymc.taboolib.string;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bkm016
 * @since 2018-04-16
 */
public class ArrayUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(T... args) {
		List<T> list = new ArrayList<>();
		for (T value : args) {
			list.add(value);
		}
		return list;
	}
	
	public static String[] addFirst(String[] args, String... value) {
		if (args.length < 1) {
			return value;
		}
		List<String> list = asList(args);
		for (int i = value.length - 1 ; i >= 0 ; i--) {
			list.add(0, value[i]);
		}
		return list.toArray(new String[0]);
	}
	
	public static String[] removeFirst(String[] args) {
		if (args.length <= 1) {
			return null;
		}
		List<String> list = asList(args);
		list.remove(0);
		return list.toArray(new String[0]);
	}
	
	public static String arrayJoin(String[] args, int start) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = start ; i < args.length ; i++) {
			stringBuilder.append(args[i]);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString().trim();
	}
}
