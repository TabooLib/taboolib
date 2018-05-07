package me.skymc.taboolib.json;

@SuppressWarnings({"rawtypes", "unchecked"})
public class XMLTokener extends JSONTokener {

    public static final java.util.HashMap entity;

    static {
        entity = new java.util.HashMap(8);
        entity.put("amp", XML.AMP);
        entity.put("apos", XML.APOS);
        entity.put("gt", XML.GT);
        entity.put("lt", XML.LT);
        entity.put("quot", XML.QUOT);
    }

    public XMLTokener(String s) {
        super(s);
    }

    public String nextCDATA() throws JSONException {
        char c;
        int i;
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = next();
            if (end()) {
                throw syntaxError("Unclosed CDATA");
            }
            sb.append(c);
            i = sb.length() - 3;
            if (i >= 0 && sb.charAt(i) == ']' &&
                    sb.charAt(i + 1) == ']' && sb.charAt(i + 2) == '>') {
                sb.setLength(i);
                return sb.toString();
            }
        }
    }

    public Object nextContent() throws JSONException {
        char c;
        StringBuffer sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if (c == 0) {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        sb = new StringBuffer();
        for (; ; ) {
            if (c == '<' || c == 0) {
                back();
                return sb.toString().trim();
            }
            if (c == '&') {
                sb.append(nextEntity(c));
            } else {
                sb.append(c);
            }
            c = next();
        }
    }

    public Object nextEntity(char ampersand) throws JSONException {
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            char c = next();
            if (Character.isLetterOrDigit(c) || c == '#') {
                sb.append(Character.toLowerCase(c));
            } else if (c == ';') {
                break;
            } else {
                throw syntaxError("Missing ';' in XML entity: &" + sb);
            }
        }
        String string = sb.toString();
        Object object = entity.get(string);
        return object != null ? object : ampersand + string + ";";
    }

    public Object nextMeta() throws JSONException {
        char c;
        char q;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped meta tag");
            case '<':
                return XML.LT;
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;
            case '"':
            case '\'':
                q = c;
                for (; ; ) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return Boolean.TRUE;
                    }
                }
            default:
                for (; ; ) {
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return Boolean.TRUE;
                    }
                    switch (c) {
                        case 0:
                        case '<':
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '"':
                        case '\'':
                            back();
                            return Boolean.TRUE;
                    }
                }
        }
    }

    public Object nextToken() throws JSONException {
        char c;
        char q;
        StringBuffer sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped element");
            case '<':
                throw syntaxError("Misplaced '<'");
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;
            case '"':
            case '\'':
                q = c;
                sb = new StringBuffer();
                for (; ; ) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return sb.toString();
                    }
                    if (c == '&') {
                        sb.append(nextEntity(c));
                    } else {
                        sb.append(c);
                    }
                }
            default:
                sb = new StringBuffer();
                for (; ; ) {
                    sb.append(c);
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return sb.toString();
                    }
                    switch (c) {
                        case 0:
                            return sb.toString();
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '[':
                        case ']':
                            back();
                            return sb.toString();
                        case '<':
                        case '"':
                        case '\'':
                            throw syntaxError("Bad character in a name");
                    }
                }
        }
    }

    public boolean skipPast(String to) throws JSONException {
        boolean b;
        char c;
        int i;
        int j;
        int offset = 0;
        int length = to.length();
        char[] circle = new char[length];

        for (i = 0; i < length; i += 1) {
            c = next();
            if (c == 0) {
                return false;
            }
            circle[i] = c;
        }
        for (; ; ) {
            j = offset;
            b = true;
            for (i = 0; i < length; i += 1) {
                if (circle[j] != to.charAt(i)) {
                    b = false;
                    break;
                }
                j += 1;
                if (j >= length) {
                    j -= length;
                }
            }
            if (b) {
                return true;
            }
            c = next();
            if (c == 0) {
                return false;
            }
            circle[offset] = c;
            offset += 1;
            if (offset >= length) {
                offset -= length;
            }
        }
    }
}
