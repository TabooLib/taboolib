package me.skymc.taboolib.string.obfuscated;

import javax.xml.bind.DatatypeConverter;

public class CT {

    public static String decode(CodeType type, String string) {
        switch (type) {
            case BASE64: {
                return new String(DatatypeConverter.parseBase64Binary(string));
            }
            case BINARY: {
                StringBuilder text = new StringBuilder();
                for (String segment : string.split(" ")) {
                    text.append(Character.toString((char) Integer.parseInt(segment, 2)));
                }
                return text.toString();
            }
        }
        return "";
	}
	
	public static String encode(CodeType type, String string) {
		switch (type) {
			case BASE64: {
				return DatatypeConverter.printBase64Binary(string.getBytes());
			}
			case BINARY: {
				StringBuilder binary = new StringBuilder();
				for (byte b: string.getBytes()) {
					int value = b;
					for (int i = 0; i < 8; i++) {
						binary.append((value & 128) == 0 ? 0: 1);
						value <<= 1;
					}
					binary.append(" ");
				}
				return binary.toString();
			}
		}
		return "";
	}

    public enum CodeType {
        BASE64,
        BINARY
	}

}
