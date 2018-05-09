package com.ilummc.tlib.bean;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class PropertyTypeAdaptor implements JsonDeserializer<Property> {

    @Override
    public Property deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
