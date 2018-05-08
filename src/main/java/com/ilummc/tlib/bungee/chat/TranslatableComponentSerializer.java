//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ilummc.tlib.bungee.chat;

import com.google.gson.*;
import com.ilummc.tlib.bungee.api.chat.BaseComponent;
import com.ilummc.tlib.bungee.api.chat.TranslatableComponent;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author md_5
 */
public class TranslatableComponentSerializer extends BaseComponentSerializer implements JsonSerializer<TranslatableComponent>, JsonDeserializer<TranslatableComponent> {

    public TranslatableComponentSerializer() {
    }

    @Override
    public TranslatableComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TranslatableComponent component = new TranslatableComponent();
        JsonObject object = json.getAsJsonObject();
        this.deserialize(object, component, context);
        component.setTranslate(object.get("translate").getAsString());
        if (object.has("with")) {
            component.setWith(Arrays.asList((BaseComponent[]) context.deserialize(object.get("with"), BaseComponent[].class)));
        }

        return component;
    }

    @Override
    public JsonElement serialize(TranslatableComponent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        this.serialize(object, src, context);
        object.addProperty("translate", src.getTranslate());
        if (src.getWith() != null) {
            object.add("with", context.serialize(src.getWith()));
        }
        return object;
    }
}
