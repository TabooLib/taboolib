package me.skymc.taboolib.string;

@Deprecated
public class PatternUtils {
	
	@Deprecated
	public static String doubleNumber = "((\\-|\\+)?\\d+(\\.\\d+)?)";
	@Deprecated
	public static String doubleNumber2 = "(\\d+(\\.\\d+)?)";
	
	public static String doubleNumber3 = "((?:\\-|\\+)?\\d+(?:\\.\\d+)?)";
	
	public static String consolidateStrings(final String[] args, final int start) {
		String ret = args[start];
		
		if (args.length > start + 1) {
			for (int i = start + 1; i < args.length; ++i) {
				ret = ret + " " + args[i];
			}
		}
		return ret;
	}
}
