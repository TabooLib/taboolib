package me.skymc.taboolib.json;

import java.util.Iterator;

@SuppressWarnings({"rawtypes"})
public class JSONML {

    private static Object parse(XMLTokener x, boolean arrayForm, JSONArray ja) throws JSONException {
        String attribute;
        char c;
        String closeTag;
        int i;
        JSONArray newja;
        JSONObject newjo;
        Object token;
        String tagName;

        while (true) {
            if (!x.more()) {
                throw x.syntaxError("Bad XML");
            }
            token = x.nextContent();
            if (token == XML.LT) {
                token = x.nextToken();
                if (token instanceof Character) {
                    if (token == XML.SLASH) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw new JSONException(
                                    "Expected a closing name instead of '" +
                                            token + "'.");
                        }
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped close tag");
                        }
                        return token;
                    } else if (token == XML.BANG) {
                        c = x.next();
                        switch (c) {
                            case '-':
                                if (x.next() == '-') {
                                    x.skipPast("-->");
                                } else {
                                    x.back();
                                }
                                break;
                            case '[':
                                token = x.nextToken();
                                if (token.equals("CDATA") && x.next() == '[') {
                                    if (ja != null) {
                                        ja.put(x.nextCDATA());
                                    }
                                } else {
                                    throw x.syntaxError("Expected 'CDATA['");
                                }
                                break;
                            default:
                                i = 1;
                                do {
                                    token = x.nextMeta();
                                    if (token == null) {
                                        throw x.syntaxError("Missing '>' after '<!'.");
                                    } else if (token == XML.LT) {
                                        i += 1;
                                    } else if (token == XML.GT) {
                                        i -= 1;
                                    }
                                } while (i > 0);
                                break;
                        }
                    } else if (token == XML.QUEST) {
                        x.skipPast("?>");
                    } else {
                        throw x.syntaxError("Misshaped tag");
                    }
                } else {
                    if (!(token instanceof String)) {
                        throw x.syntaxError("Bad tagName '" + token + "'.");
                    }
                    tagName = (String) token;
                    newja = new JSONArray();
                    newjo = new JSONObject();
                    if (arrayForm) {
                        newja.put(tagName);
                        if (ja != null) {
                            ja.put(newja);
                        }
                    } else {
                        newjo.put("tagName", tagName);
                        if (ja != null) {
                            ja.put(newjo);
                        }
                    }
                    token = null;
                    for (; ; ) {
                        if (token == null) {
                            token = x.nextToken();
                        }
                        if (token == null) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (!(token instanceof String)) {
                            break;
                        }
                        attribute = (String) token;
                        if (!arrayForm && ("tagName".equals(attribute) || "childNode".equals(attribute))) {
                            throw x.syntaxError("Reserved attribute.");
                        }
                        token = x.nextToken();
                        if (token == XML.EQ) {
                            token = x.nextToken();
                            if (!(token instanceof String)) {
                                throw x.syntaxError("Missing value");
                            }
                            newjo.accumulate(attribute, XML.stringToValue((String) token));
                            token = null;
                        } else {
                            newjo.accumulate(attribute, "");
                        }
                    }
                    if (arrayForm && newjo.length() > 0) {
                        newja.put(newjo);
                    }
                    if (token == XML.SLASH) {
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (ja == null) {
                            if (arrayForm) {
                                return newja;
                            } else {
                                return newjo;
                            }
                        }
                    } else {
                        if (token != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        closeTag = (String) parse(x, arrayForm, newja);
                        if (closeTag != null) {
                            if (!closeTag.equals(tagName)) {
                                throw x.syntaxError("Mismatched '" + tagName +
                                        "' and '" + closeTag + "'");
                            }
                            if (!arrayForm && newja.length() > 0) {
                                newjo.put("childNodes", newja);
                            }
                            if (ja == null) {
                                if (arrayForm) {
                                    return newja;
                                } else {
                                    return newjo;
                                }
                            }
                        }
                    }
                }
            } else {
                if (ja != null) {
                    ja.put(token instanceof String
                            ? XML.stringToValue((String) token)
                            : token);
                }
            }
        }
    }

    public static JSONArray toJSONArray(String string) throws JSONException {
        return toJSONArray(new XMLTokener(string));
    }

    public static JSONArray toJSONArray(XMLTokener x) throws JSONException {
        return (JSONArray) parse(x, true, null);
    }

    public static JSONObject toJSONObject(XMLTokener x) throws JSONException {
        return (JSONObject) parse(x, false, null);
    }

    public static JSONObject toJSONObject(String string) throws JSONException {
        return toJSONObject(new XMLTokener(string));
    }

    public static String toString(JSONArray ja) throws JSONException {
        int i;
        JSONObject jo;
        String key;
        Iterator keys;
        int length;
        Object object;
        StringBuilder sb = new StringBuilder();
        String tagName;
        String value;
        tagName = ja.getString(0);
        XML.noSpace(tagName);
        tagName = XML.escape(tagName);
        sb.append('<');
        sb.append(tagName);

        object = ja.opt(1);
        if (object instanceof JSONObject) {
            i = 2;
            jo = (JSONObject) object;
            keys = jo.keys();
            while (keys.hasNext()) {
                key = keys.next().toString();
                XML.noSpace(key);
                value = jo.optString(key);
                if (value != null) {
                    sb.append(' ');
                    sb.append(XML.escape(key));
                    sb.append('=');
                    sb.append('"');
                    sb.append(XML.escape(value));
                    sb.append('"');
                }
            }
        } else {
            i = 1;
        }
        length = ja.length();
        if (i >= length) {
            sb.append('/');
            sb.append('>');
        } else {
            sb.append('>');
            do {
                object = ja.get(i);
                i += 1;
                if (object != null) {
                    if (object instanceof String) {
                        sb.append(XML.escape(object.toString()));
                    } else if (object instanceof JSONObject) {
                        sb.append(toString((JSONObject) object));
                    } else if (object instanceof JSONArray) {
                        sb.append(toString((JSONArray) object));
                    }
                }
            } while (i < length);
            sb.append('<');
            sb.append('/');
            sb.append(tagName);
            sb.append('>');
        }
        return sb.toString();
    }

    public static String toString(JSONObject jo) throws JSONException {
        StringBuilder sb = new StringBuilder();
        int i;
        JSONArray ja;
        String key;
        Iterator keys;
        int length;
        Object object;
        String tagName;
        String value;
        tagName = jo.optString("tagName");
        if (tagName == null) {
            return XML.escape(jo.toString());
        }
        XML.noSpace(tagName);
        tagName = XML.escape(tagName);
        sb.append('<');
        sb.append(tagName);
        keys = jo.keys();
        while (keys.hasNext()) {
            key = keys.next().toString();
            if (!"tagName".equals(key) && !"childNodes".equals(key)) {
                XML.noSpace(key);
                value = jo.optString(key);
                if (value != null) {
                    sb.append(' ');
                    sb.append(XML.escape(key));
                    sb.append('=');
                    sb.append('"');
                    sb.append(XML.escape(value));
                    sb.append('"');
                }
            }
        }
        ja = jo.optJSONArray("childNodes");
        if (ja == null) {
            sb.append('/');
            sb.append('>');
        } else {
            sb.append('>');
            length = ja.length();
            for (i = 0; i < length; i += 1) {
                object = ja.get(i);
                if (object != null) {
                    if (object instanceof String) {
                        sb.append(XML.escape(object.toString()));
                    } else if (object instanceof JSONObject) {
                        sb.append(toString((JSONObject) object));
                    } else if (object instanceof JSONArray) {
                        sb.append(toString((JSONArray) object));
                    } else {
                        sb.append(object.toString());
                    }
                }
            }
            sb.append('<');
            sb.append('/');
            sb.append(tagName);
            sb.append('>');
        }
        return sb.toString();
    }
}
