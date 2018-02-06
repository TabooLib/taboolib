package me.skymc.taboolib.string.obfuscated;

@Deprecated
public class FZ {
	
    public static String toByte(final String string) {
        final StringBuilder sb = new StringBuilder("");
        final byte[] bs = string.getBytes();
        byte[] array;
        for (int length = (array = bs).length, i = 0; i < length; ++i) {
            final byte b = array[i];
            sb.append(String.valueOf(b) + "#");
        }
        return sb.toString();
    }
    
    public static byte[] getByte(final String string) {
        final String[] value = string.split("#");
        final byte[] bs = new byte[value.length];
        for (int i = 0; i < value.length; ++i) {
            bs[i] = Byte.valueOf(value[i]);
        }
        return bs;
    }
    
    public static String unByte(final String string) {
        final String[] value = string.split("#");
        final byte[] bs = new byte[value.length];
        for (int i = 0; i < value.length; ++i) {
            bs[i] = Byte.valueOf(value[i]);
        }
        return new String(bs);
    }
    
    public static String load(final String string, final int power) {
        final StringBuilder sb = new StringBuilder("");
        char[] charArray;
        for (int length = (charArray = string.toCharArray()).length, j = 0; j < length; ++j) {
            int i;
            final char c = (char)(i = charArray[j]);
            i *= power;
            sb.append(String.valueOf(i) + "#");
        }
        return sb.toString();
    }
    
    public static String load2(final String string, final int power) {
        final String[] value = string.split("#");
        final StringBuilder sb = new StringBuilder("");
        String[] array;
        for (int length = (array = value).length, j = 0; j < length; ++j) {
            final String c = array[j];
            int i = Integer.valueOf(c);
            i /= power;
            sb.append((char)i);
        }
        return sb.toString();
    }
}
