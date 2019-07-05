//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.izzel.taboolib.util.chat;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author md_5
 */
public class TextComponentSerializer extends BaseComponentSerializer implements JsonSerializer<TextComponent>, JsonDeserializer<TextComponent> {

    public TextComponentSerializer() {
    }

    @Override
    public TextComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TextComponent component = new TextComponent();
        JsonObject object = json.getAsJsonObject();
        this.deserialize(object, component, context);
        component.setText(object.get("text").getAsString());
        return component;
    }

    @Override
    public JsonElement serialize(TextComponent src, Type typeOfSrc, JsonSerializationContext context) {
        List<BaseComponent> extra = src.getExtra();
        JsonObject object = new JsonObject();
        if (src.hasFormatting() || extra != null && !extra.isEmpty()) {
            this.serialize(object, src, context);
        }
        object.addProperty("text", src.getText());
        return object;
    }
}
