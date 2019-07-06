package io.izzel.taboolib.util.serialize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.util.ArrayUtil;

import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-04-01 17:49
 */
public class TSerializerExample {

    public static void main(String[] args) {
        // 创建对象
        SimpleData data = new SimpleData();
        // 修改参数
        data.number = 9999;
        data.list.addAll(ArrayUtil.asList(111D, 222D));
        data.map.putAll(ImmutableMap.of("a", "b", "c", "d"));
        // 序列化
        String value = data.write();
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
        private String text = "123";
        private int number = 100;

        /**
         * 包含基本类型的容器需要通过标注来完成序列化
         */
        @TSerializeCollection
        private List<Double> list = Lists.newArrayList();

        @TSerializeMap
        private Map<String, String> map = Maps.newHashMap();

        /**
         * 跳过序列化
         */
        @DoNotSerialize
        private String ignoreSerialize = "aaa";

        @Override
        public String toString() {
            return "SimpleData{" +
                    "text='" + text + '\'' +
                    ", number=" + number +
                    ", list=" + list +
                    ", map=" + map +
                    ", ignoreSerialize='" + ignoreSerialize + '\'' +
                    '}';
        }
    }
}
