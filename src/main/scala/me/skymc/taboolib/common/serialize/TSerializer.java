package me.skymc.taboolib.common.serialize;

import ch.njol.skript.classes.ConfigurationSerializer;
import com.google.gson.*;
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
                        Optional<TSerializerElementGeneral> serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(declaredField.getType())).findFirst();
                        if (serializer.isPresent()) {
                            declaredField.set(serializable, serializer.get().getSerializer().read(jsonElementEntry.getValue().getAsString()));
                        } else {
                            serializable.read(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
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
        JsonObject jsonObject = new JsonObject();
        JsonObject serializeObject = new JsonObject();
        for (Field declaredField : serializable.getClass().getDeclaredFields()) {
            try {
                if (!Modifier.isStatic(declaredField.getModifiers())) {
                    declaredField.setAccessible(true);
                    Optional<TSerializerElementGeneral> serializer = Arrays.stream(TSerializerElementGeneral.values()).filter(serializerElements -> serializerElements.getSerializer().matches(declaredField.getType())).findFirst();
                    Object o = declaredField.get(serializable);
                    if (o == null) {
                        continue;
                    }
                    if (serializer.isPresent()) {
                        serializeObject.addProperty(declaredField.getName(), serializer.get().getSerializer().write(o));
                    } else {
                        Optional.ofNullable(serializable.write(declaredField.getName(), o)).ifPresent(value -> serializeObject.addProperty(declaredField.getName(), value));
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
}
