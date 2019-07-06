package me.skymc.taboolib.common.serialize;

import ch.njol.skript.classes.ConfigurationSerializer;
import com.google.common.collect.Maps;
import com.google.gson.*;
import me.skymc.taboolib.common.util.SimpleReflection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-03-08 17:31
 */
public class TSerializer {

    private static Map<String, TSerializerElement> generated = Maps.newHashMap();

    public static <T extends ConfigurationSerializable> T read(String value, Class<T> type) {
        return ConfigurationSerializer.deserializeCS(value, type);
    }

    public static TSerializable read(TSerializable serializable, String serializedString) {
        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(serializedString);
            if (jsonObject.has("serializeObject")) {
                JsonObject serializeObject = jsonObject.getAsJsonObject("serializeObject");
                for (Map.Entry<String, JsonElement> jsonElementEntry : serializeObject.entrySet()) {
                    try {
                        Field declaredField = serializable.getClass().getDeclaredField(jsonElementEntry.getKey());
                        declaredField.setAccessible(true);
                        if (declaredField.isAnnotationPresent(DoNotSerialize.class)) {
                            continue;
                        }
                        // Serializable
                        if (declaredField.isAnnotationPresent(TSerializeCustom.class) && TSerializable.class.isAssignableFrom(declaredField.getType())) {
                            declaredField.set(serializable, generateElement((Class<? extends TSerializable>) declaredField.getType()).read(jsonElementEntry.getValue().getAsString()));
                        }
                        // List
                        else if (declaredField.isAnnotationPresent(TSerializeCollection.class) && Collection.class.isAssignableFrom(declaredField.getType())) {
                            Class listType = SimpleReflection.getListType(declaredField);
                            if (listType == null) {
                                serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                                continue;
                            }
                            TSerializerElementGeneral serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(listType)).findFirst().orElse(null);
                            if (serializer == null) {
                                serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                            } else {
                                readCollection((Collection) declaredField.get(serializable), jsonElementEntry.getValue().getAsString(), checkCustom(listType, serializer));
                            }
                        }
                        // Map
                        else if (declaredField.isAnnotationPresent(TSerializeMap.class) && Map.class.isAssignableFrom(declaredField.getType())) {
                            Class[] mapType = SimpleReflection.getMapType(declaredField);
                            if (mapType == null) {
                                serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                                continue;
                            }
                            TSerializerElementGeneral serializerK = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(mapType[0])).findFirst().orElse(null);
                            TSerializerElementGeneral serializerV = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(mapType[1])).findFirst().orElse(null);
                            if (serializerK == null || serializerV == null) {
                                serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                            } else {
                                readMap((Map) declaredField.get(serializable), jsonElementEntry.getValue().getAsString(), checkCustom(mapType[0], serializerK), checkCustom(mapType[1], serializerV));
                            }
                        }
                        // 未声明类型
                        else {
                            TSerializerElementGeneral serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(declaredField.getType())).findFirst().orElse(null);
                            if (serializer == null) {
                                serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                            } else {
                                declaredField.set(serializable, serializer.getSerializer().read(jsonElementEntry.getValue().getAsString()));
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return serializable;
    }

    public static String write(ConfigurationSerializable serializable) {
        return ConfigurationSerializer.serializeCS(serializable);
    }

    public static String write(TSerializable serializable) {
        SimpleReflection.checkAndSave(serializable.getClass());
        JsonObject jsonObject = new JsonObject();
        JsonObject serializeObject = new JsonObject();
        for (Field declaredField : serializable.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            try {
                if (!declaredField.isAnnotationPresent(DoNotSerialize.class) && !Modifier.isStatic(declaredField.getModifiers())) {
                    Object fieldObject = declaredField.get(serializable);
                    if (fieldObject == null) {
                        continue;
                    }
                    // Serializable
                    if (declaredField.isAnnotationPresent(TSerializeCustom.class) && TSerializable.class.isAssignableFrom(declaredField.getType())) {
                        serializeObject.addProperty(declaredField.getName(), generateElement((Class<? extends TSerializable>) declaredField.getType()).write(fieldObject));
                    }
                    // List
                    else if (declaredField.isAnnotationPresent(TSerializeCollection.class) && Collection.class.isAssignableFrom(declaredField.getType())) {
                        Class listType = SimpleReflection.getListType(declaredField);
                        if (listType == null) {
                            Optional.ofNullable(serializable.write(declaredField.getName(), fieldObject)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
                            continue;
                        }
                        TSerializerElementGeneral serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(listType)).findFirst().orElse(null);
                        if (serializer == null) {
                            Optional.ofNullable(serializable.write(declaredField.getName(), fieldObject)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
                        } else {
                            serializeObject.addProperty(declaredField.getName(), writeCollection((Collection) fieldObject, checkCustom(listType, serializer)));
                        }
                    }
                    // Map
                    else if (declaredField.isAnnotationPresent(TSerializeMap.class) && Map.class.isAssignableFrom(declaredField.getType())) {
                        Class[] mapType = SimpleReflection.getMapType(declaredField);
                        if (mapType == null) {
                            Optional.ofNullable(serializable.write(declaredField.getName(), fieldObject)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
                            continue;
                        }
                        TSerializerElementGeneral serializerK = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(mapType[0])).findFirst().orElse(null);
                        TSerializerElementGeneral serializerV = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(mapType[1])).findFirst().orElse(null);
                        if (serializerK == null || serializerV == null) {
                            Optional.ofNullable(serializable.write(declaredField.getName(), fieldObject)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
                        } else {
                            serializeObject.addProperty(declaredField.getName(), writeMap((Map) fieldObject, checkCustom(mapType[0], serializerK), checkCustom(mapType[1], serializerV)));
                        }
                    }
                    // 未声明类型
                    else {
                        TSerializerElementGeneral serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(declaredField.getType())).findFirst().orElse(null);
                        if (serializer == null) {
                            Optional.ofNullable(serializable.write(declaredField.getName(), fieldObject)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
                        } else {
                            serializeObject.addProperty(declaredField.getName(), serializer.getSerializer().write(fieldObject));
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        jsonObject.add("serializeObject", serializeObject);
        return jsonObject.toString();
    }

    public static void readMap(Map map, String serializedString, TSerializerElementGeneral elementKey, TSerializerElementGeneral elementValue) {
        readMap(map, serializedString, elementKey.getSerializer(), elementValue.getSerializer());
    }

    public static void readMap(Map map, String serializedString, TSerializerElement elementKey, TSerializerElement elementValue) {
        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(serializedString);
            for (Map.Entry<String, JsonElement> jsonElementEntry : jsonObject.entrySet()) {
                try {
                    map.put(elementKey.read(jsonElementEntry.getKey()), elementValue.read(jsonElementEntry.getValue().getAsString()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String writeMap(Map<?, ?> map, TSerializerElementGeneral elementKey, TSerializerElementGeneral elementValue) {
        return writeMap(map, elementKey.getSerializer(), elementValue.getSerializer());
    }

    public static String writeMap(Map<?, ?> map, TSerializerElement elementKey, TSerializerElement elementValue) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            try {
                jsonObject.addProperty(elementKey.write(entry.getKey()), elementValue.write(entry.getValue()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    public static void readCollection(Collection collection, String serializedString, TSerializerElementGeneral elementValue) {
        readCollection(collection, serializedString, elementValue.getSerializer());
    }

    public static void readCollection(Collection collection, String serializedString, TSerializerElement elementValue) {
        try {
            JsonArray jsonArray = (JsonArray) new JsonParser().parse(serializedString);
            for (JsonElement jsonElement : jsonArray) {
                try {
                    collection.add(elementValue.read(jsonElement.getAsString()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String writeCollection(Collection collection, TSerializerElementGeneral elementValue) {
        return writeCollection(collection, elementValue.getSerializer());
    }

    public static String writeCollection(Collection collection, TSerializerElement elementValue) {
        JsonArray jsonArray = new JsonArray();
        for (Object object : collection) {
            try {
                jsonArray.add(new JsonPrimitive(elementValue.write(object)));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    public static String serializeCS(ConfigurationSerializable o) {
        YamlConfiguration y = new YamlConfiguration();
        y.set("value", o);
        return y.saveToString();
    }

    public static <T extends ConfigurationSerializable> T deserializeCS(String s, Class<T> c) {
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.loadFromString(s);
        } catch (InvalidConfigurationException var4) {
            return null;
        }
        Object o = y.get("value");
        return !c.isInstance(o) ? null : (T) o;
    }

    public static TSerializerElement generateElement(Class<? extends TSerializable> serializable) {
        return generated.computeIfAbsent(serializable.getName(), n -> new TSerializerElement() {

            @Override
            public Object read(String value) {
                try {
                    return serializable.newInstance().read(value);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return null;
            }

            @Override
            public String write(Object value) {
                return ((TSerializable) value).write();
            }

            @Override
            public boolean matches(Class objectClass) {
                return true;
            }
        });
    }

    private static TSerializerElement checkCustom(Class type, TSerializerElementGeneral serializer) {
        return serializer == TSerializerElementGeneral.CUSTOM ? generateElement(type) : serializer.getSerializer();
    }
}
