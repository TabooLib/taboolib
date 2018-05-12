package me.skymc.taboolib.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bkm016
 * @since 2018-04-16
 */
public class ArrayUtils {
	
	public static <T> List<T> asList(T... args) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, args);
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
			return new String[0];
		}
		List<String> list = asList(args);
		list.remove(0);
		return list.toArray(new String[0]);
	}
	
	public static String arrayJoin(String[] args, int start) {
		return IntStream.range(start, args.length).mapToObj(i -> args[i] + " ").collect(Collectors.joining()).trim();
	}
}
