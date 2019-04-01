package me.skymc.taboolib.common.serialize;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-04-01 17:49
 */
public class TSerializerExample {

    public static void main(String[] args) {
        // 创建对象
        SimpleData date = new SimpleData();
        // 修改参数
        date.number = 100;
        // 序列化
        String value = date.write();
        // 打印
        System.out.println(value);
        // 创建新的对象
        SimpleData dataCopy = new SimpleData();
        // 反序列化
        dataCopy.read(value);
        // 打印
        System.out.println(dataCopy);
    }

    /**
     * 创建序列化类
     * 实现 TSerializable 接口
     */
    public static class SimpleData implements TSerializable {

        /**
         * 基本类型不需要手动进行序列化
         * 包含: String、int、short、long、double、float、boolean、ItemStack、Location
         */
        private String text;
        private int number;
        /**
         * 特殊类型需要进行手动序列化
         * 本工具提供了基本容器的序列化方法
         */
        private List<Double> list = Lists.newArrayList();

        /**
         * 基本类型不会执行以下两个方法
         * 由 TSerializer 自动进行序列化和反序列化步骤
         */
        @Override
        public void read(String fieldName, String value) {
            switch (fieldName) {
                case "list": {
                    // List 类型可以直接通过 TSerializer 提供的预设方法进行反序列化
                    TSerializer.readCollection(list, value, TSerializerElementGeneral.DOUBLE);
                    break;
                }
                default:
            }
        }

        @Override
        public String write(String fieldName, Object value) {
            switch (fieldName) {
                case "list": {
                    // 序列化同理
                    return TSerializer.writeCollection((Collection) value, TSerializerElementGeneral.DOUBLE);
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "SimpleData{" +
                    "text='" + text + '\'' +
                    ", number=" + number +
                    ", list=" + list +
                    '}';
        }
    }
}
