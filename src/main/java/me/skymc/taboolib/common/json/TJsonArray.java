package me.skymc.taboolib.common.json;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @Author 坏黑
 * @Since 2018-10-27 23:46
 */
public class TJsonArray implements Iterable<TJsonObject> {

    private JsonArray jsonArray;

    TJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    /**
     * 从 Json 原代码中获取
     *
     * @param json 源代码
     * @return {@link TJsonArray}
     * @throws JsonParseException    if the specified text is not valid JSON
     * @throws IllegalStateException This is not a JSON Array.
     */
    public static TJsonArray fromJson(String json) throws JsonParseException, IllegalStateException {
        return new TJsonArray(new JsonParser().parse(json).getAsJsonArray());
    }

    /**
     * 从 JsonArray 创建
     *
     * @param jsonArray JsonArray 对象
     * @return {@link TJsonObject}
     */
    public static TJsonArray fromJsonArray(JsonArray jsonArray) {
        return new TJsonArray(jsonArray);
    }

    /**
     * 添加成员
     *
     * @param obj 成员
     */
    public void add(TJsonObject obj) {
        jsonArray.add(obj.asOriginJsonElement());
    }

    /**
     * 添加成员
     *
     * @param obj 成员
     */
    public void add(Boolean obj) {
        jsonArray.add(obj);
    }

    /**
     * 添加成员
     *
     * @param obj 成员
     */
    public void add(Character obj) {
        jsonArray.add(obj);
    }

    /**
     * 添加成员
     *
     * @param obj 成员
     */
    public void add(Number obj) {
        jsonArray.add(obj);
    }

    /**
     * 添加成员
     *
     * @param obj 成员
     */
    public void add(String obj) {
        jsonArray.add(obj);
    }

    /**
     * 添加所有成员
     *
     * @param array 成员集合
     */
    public void addAll(TJsonArray array) {
        jsonArray.add(array.asOriginJsonArray());
    }

    /**
     * 设置成员
     *
     * @param index 位置
     * @param obj   成员
     */
    public void set(int index, TJsonObject obj) {
        jsonArray.set(index, obj.asOriginJsonElement());
    }

    /**
     * 设置成员
     *
     * @param index 位置
     * @param obj   成员
     */
    public void set(int index, Boolean obj) {
        jsonArray.set(index, new JsonPrimitive(obj));
    }

    /**
     * 设置成员
     *
     * @param index 位置
     * @param obj   成员
     */
    public void set(int index, Number obj) {
        jsonArray.set(index, new JsonPrimitive(obj));
    }

    /**
     * 设置成员
     *
     * @param index 位置
     * @param obj   成员
     */
    public void set(int index, String obj) {
        jsonArray.set(index, new JsonPrimitive(obj));
    }

    /**
     * 移除成员
     *
     * @param obj 成员
     */
    public void remove(JsonElement obj) {
        jsonArray.remove(obj);
    }

    /**
     * 移除成员
     *
     * @param obj 成员
     */
    public void remove(Boolean obj) {
        jsonArray.remove(new JsonPrimitive(obj));
    }

    /**
     * 移除成员
     *
     * @param obj 成员
     */
    public void remove(Number obj) {
        jsonArray.remove(new JsonPrimitive(obj));
    }

    /**
     * 移除成员
     *
     * @param obj 成员
     */
    public void remove(String obj) {
        jsonArray.remove(new JsonPrimitive(obj));
    }

    /**
     * 含有成员
     *
     * @param obj 成员
     * @return boolean
     */
    public boolean contains(JsonElement obj) {
        return jsonArray.contains(obj);
    }

    /**
     * 含有成员
     *
     * @param obj 成员
     * @return boolean
     */
    public boolean contains(Boolean obj) {
        return jsonArray.contains(new JsonPrimitive(obj));
    }

    /**
     * 含有成员
     *
     * @param obj 成员
     * @return boolean
     */
    public boolean contains(Number obj) {
        return jsonArray.contains(new JsonPrimitive(obj));
    }

    /**
     * 含有成员
     *
     * @param obj 成员
     * @return boolean
     */
    public boolean contains(String obj) {
        return jsonArray.contains(new JsonPrimitive(obj));
    }

    /**
     * 获取成员，默认值：null
     *
     * @param index 序号
     * @return {@link TJsonObject}
     */
    public TJsonObject getJsonObject(int index) {
        return jsonArray.get(index).isJsonObject() ? TJsonObject.fromJsonObject(jsonArray.get(index).getAsJsonObject()) : null;
    }

    /**
     * 获取成员，默认值：null
     *
     * @param index 序号
     * @return {@link TJsonArray}
     */
    public TJsonArray getJsonArray(int index) {
        return jsonArray.get(index).isJsonArray() ? TJsonArray.fromJsonArray(jsonArray.get(index).getAsJsonArray()) : null;
    }

    /**
     * 获取成员，默认值：false
     *
     * @param index 序号
     * @return boolean
     */
    public Boolean getBoolean(int index) {
        return jsonArray.get(index).isJsonPrimitive() && jsonArray.get(index).getAsBoolean();
    }

    /**
     * 获取成员
     *
     * @param index 序号
     * @param def   默认值
     * @return boolean
     */
    public Boolean getBoolean(int index, boolean def) {
        return jsonArray.get(index).isJsonPrimitive() ? jsonArray.get(index).getAsBoolean() : def;
    }

    /**
     * 获取成员，默认值：0
     *
     * @param index 序号
     * @return number
     */
    public Number getNumber(int index) {
        return jsonArray.get(index).isJsonPrimitive() ? jsonArray.get(index).getAsNumber() : 0;
    }

    /**
     * 获取成员
     *
     * @param index 序号
     * @param def   默认值
     * @return number
     */
    public Number getNumber(int index, Number def) {
        return jsonArray.get(index).isJsonPrimitive() ? jsonArray.get(index).getAsNumber() : def;
    }

    /**
     * 获取成员，默认值：null
     *
     * @param index 序号
     * @return string
     */
    public String getString(int index) {
        return jsonArray.get(index).isJsonPrimitive() ? jsonArray.get(index).getAsString() : null;
    }

    /**
     * 获取成员
     *
     * @param index 序号
     * @param def   默认值
     * @return string
     */
    public String getString(int index, String def) {
        return jsonArray.get(index).isJsonPrimitive() ? jsonArray.get(index).getAsString() : def;
    }

    /**
     * 成员数量
     *
     * @return int
     */
    public int size() {
        return jsonArray.size();
    }

    /**
     * 转换为 JsonArray 类型
     *
     * @return {@link JsonArray}
     */
    public JsonArray asOriginJsonArray() {
        return jsonArray;
    }

    @Override
    public Iterator<TJsonObject> iterator() {
        List<TJsonObject> jsonObjectList = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            jsonObjectList.add(TJsonObject.fromJsonObject(jsonElement));
        }
        return jsonObjectList.iterator();
    }

    @Override
    public void forEach(Consumer<? super TJsonObject> action) {
        List<TJsonObject> jsonObjectList = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            jsonObjectList.add(TJsonObject.fromJsonObject(jsonElement));
        }
        jsonObjectList.forEach(action);
    }

    @Override
    public Spliterator<TJsonObject> spliterator() {
        List<TJsonObject> jsonObjectList = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            jsonObjectList.add(TJsonObject.fromJsonObject(jsonElement));
        }
        return jsonObjectList.spliterator();
    }

    @Override
    public String toString() {
        return jsonArray.toString();
    }

}
