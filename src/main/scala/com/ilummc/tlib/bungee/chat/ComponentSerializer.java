package com.ilummc.tlib.bungee.chat;

import com.google.gson.*;
import com.ilummc.tlib.bungee.api.chat.BaseComponent;
import com.ilummc.tlib.bungee.api.chat.TextComponent;
import com.ilummc.tlib.bungee.api.chat.TranslatableComponent;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author md_5
 */
public class ComponentSerializer implements JsonDeserializer<BaseComponent> {

    private final static JsonParser JSON_PARSER = new JsonParser();
    private final static Gson gson = new GsonBuilder().
            registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).
            registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).
            registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).
            create();

    public final static ThreadLocal<HashSet<BaseComponent>> serializedComponents = new ThreadLocal<>();

    public static BaseComponent[] parse(String json) {
        JsonElement jsonElement = JSON_PARSER.parse(json);

        if (jsonElement.isJsonArray()) {
            return gson.fromJson(jsonElement, BaseComponent[].class);
        } else {
            return new BaseComponent[]{gson.fromJson(jsonElement, BaseComponent.class)};
        }
    }

    public static String toString(BaseComponent component) {
        return gson.toJson(component);
    }

    public static String toString(BaseComponent... components) {
        if (components.length == 1) {
            return gson.toJson(components[0]);
        } else {
            return gson.toJson(new TextComponent(components));
        }
    }

    @Override
    public BaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new TextComponent(json.getAsString());
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("translate")) {
            return context.deserialize(json, TranslatableComponent.class);
        }
        return context.deserialize(json, TextComponent.class);
    }
}
