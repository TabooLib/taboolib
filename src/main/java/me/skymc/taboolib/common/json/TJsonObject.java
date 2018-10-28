package me.skymc.taboolib.common.json;

import com.google.gson.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2018-10-27 23:06
 */
public class TJsonObject {

    private JsonElement jsonObject;

    TJsonObject(JsonElement jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * 从 Json 原代码中获取
     *
     * @param json 源代码
     * @return {@link TJsonObject}
     * @throws JsonParseException if the specified text is not valid JSON
     */
    public static TJsonObject fromJson(String json) throws JsonParseException {
        return new TJsonObject(new JsonParser().parse(json));
    }

    /**
     * 从 JsonElement 创建
     *
     * @param jsonElement JsonElement 对象
     * @return {@link TJsonObject}
     */
    public static TJsonObject fromJsonObject(JsonElement jsonElement) {
        return new TJsonObject(jsonElement);
    }

    /**
     * 是否含有该节点
     *
     * @param path 地址
     * @return boolean
     */
    public boolean contains(String path) {
        return get(path) != null;
    }

    /**
     * 获取文本，默认值：空
     *
     * @param path 地址
     * @return String
     */
    public String getString(String path) {
        return getString(path, "");
    }

    /**
     * 获取文本
     *
     * @param path 地址
     * @param def  默认值
     * @return String
     */
    public String getString(String path, String def) {
        JsonElement jsonElement = get(path);
        return !(jsonElement instanceof JsonPrimitive) || jsonElement == null ? def : jsonElement.getAsString();
    }

    /**
     * 获取数字，默认值：0
     *
     * @param path 地址
     * @return Number
     */
    public Number getNumber(String path) {
        return getNumber(path, 0);
    }

    /**
     * 获取数字
     *
     * @param path 地址
     * @param def  默认值
     * @return Number
     */
    public Number getNumber(String path, Number def) {
        JsonElement jsonElement = get(path);
        return !(jsonElement instanceof JsonPrimitive) || jsonElement == null ? def : jsonElement.getAsNumber();
    }

    /**
     * 获取布尔值，默认值：false
     *
     * @param path 地址
     * @return boolean
     */
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    /**
     * 获取布尔值
     *
     * @param path 地址
     * @param def  默认值
     * @return boolean
     */
    public boolean getBoolean(String path, boolean def) {
        JsonElement jsonElement = get(path);
        return !(jsonElement instanceof JsonPrimitive) || jsonElement == null ? def : jsonElement.getAsBoolean();
    }

    /**
     * 获取 TJsonObject 对象，默认值：null
     *
     * @param path 地址
     * @return {@link TJsonObject}
     */
    public TJsonObject getJsonObject(String path) {
        JsonElement jsonElement = get(path);
        return !(jsonElement instanceof JsonObject) || jsonElement == null ? null : TJsonObject.fromJsonObject(jsonElement);
    }

    /**
     * 获取 TJsonArray 对象，默认值：null
     *
     * @param path 地址
     * @return {@link TJsonArray}
     */
    public TJsonArray getJsonArray(String path) {
        JsonElement jsonElement = get(path);
        return !(jsonElement instanceof JsonArray) || jsonElement == null ? null : TJsonArray.fromJsonArray((JsonArray) jsonElement);
    }

    /**
     * 获取所有成员
     *
     * @return {@link Map.Entry}
     */
    public Set<Map.Entry<String, TJsonObject>> entrySet() {
        return !(jsonObject instanceof JsonObject) ? new HashSet<>() : ((JsonObject) jsonObject).entrySet().stream().map(jsonElementEntry -> new HashMap.SimpleEntry<>(jsonElementEntry.getKey(), fromJsonObject(jsonElementEntry.getValue()))).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 获取所有键
     *
     * @return {@link Set}
     */
    public Set<String> keySet() {
        return !(jsonObject instanceof JsonObject) ? new HashSet<>() : ((JsonObject) jsonObject).entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 获取所有值
     *
     * @return {@link Collection}
     */
    public Collection<TJsonObject> values() {
        return !(jsonObject instanceof JsonObject) ? new ArrayList<>() : ((JsonObject) jsonObject).entrySet().stream().map(jsonElementEntry -> new TJsonObject(jsonElementEntry.getValue())).collect(Collectors.toList());
    }

    /**
     * 是否为 JsonObject 类型
     *
     * @return boolean
     */
    public boolean isJsonObject() {
        return jsonObject instanceof JsonObject;
    }

    /**
     * 是否为 JsonArray 类型
     *
     * @return boolean
     */
    public boolean isJsonArray() {
        return jsonObject instanceof JsonArray;
    }

    /**
     * 是否为 JsonPrimitive 类型
     *
     * @return boolean
     */
    public boolean isJsonPrimitive() {
        return jsonObject instanceof JsonPrimitive;
    }

    /**
     * 转换为 JsonObject 类型
     *
     * @return {@link JsonObject}
     */
    public JsonObject asOriginJsonObject() {
        return (JsonObject) jsonObject;
    }

    /**
     * 转换为 JsonArray 类型
     *
     * @return {@link JsonArray}
     */
    public JsonArray asOriginJsonArray() {
        return (JsonArray) jsonObject;
    }

    /**
     * 转换为 JsonElement 类型
     *
     * @return {@link JsonElement}
     */
    public JsonElement asOriginJsonElement() {
        return jsonObject;
    }

    /**
     * 转换为 JsonPrimitive 类型
     *
     * @return {@link JsonPrimitive}
     */
    public JsonPrimitive asOriginJsonPrimitive() {
        return (JsonPrimitive) jsonObject;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private JsonElement get(String path) {
        JsonElement subElement = jsonObject;
        for (String p : path.split("/")) {
            if (subElement instanceof JsonObject && ((JsonObject) subElement).has(p)) {
                subElement = ((JsonObject) subElement).get(p);
            } else {
                return null;
            }
        }
        return subElement;
    }
}
