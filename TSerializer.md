# TSerializer
轻便的序列化 & 反序列化工具

## 使用效果
```java
public class SimpleData {

    private String text = "123";
    private int number = 100;
    private List<Double> list = Lists.newArrayList(1.0, 2.0, 3.0);
    
}
```
经过序列化后
```json
{"serializeObject":{"text":"123","number":"100","list":"[\"1.0\",\"2.0\",\"3.0\"]"}}
```
并可以通过序列化后的字符串 **反向** 创建对象

## 使用方法
将需要进行序列化的类实现 **TSerializable** 接口
```java
public class SimpleData implements TSerializable {

    private String text = "123";
    private int number = 100;
    private List<Double> list = Lists.newArrayList(1.0, 2.0, 3.0);
    
}
```
基本类型 **不需要** 手动进行序列化  
例如: `String、int、short、long、double、float、boolean、ItemStack、Location`   
  
其他类型则需要进行手动序列化，**TSerializer** 类中提供了两种容器的序列化方法  
如果类中有需要进行手动序列化的对象，则需要重写以下两个方法
```java
public class SimpleData implements TSerializable {

    private String text = "123";
    private int number = 100;
    private List<Double> list = Lists.newArrayList(1.0, 2.0, 3.0);
    
    @Override
    public String write(String fieldName, Object value) {
        // 无法自动进行序列化的对象将会被传递到这个方法中
        // 需要进行手动序列化
        switch (fieldName) {
            case "list": {
                return TSerializer.writeCollection((List) value, TSerializerElementGeneral.DOUBLE);
            }
        }
        return null;
    }
    
    @Override
    public void read(String fieldName, String value) {
        switch (fieldName) {
            case "list": {
                TSerializer.readCollection(list, value, TSerializerElementGeneral.DOUBLE);
                break;
            }
            default:
        }
    }
}
```

## 序列化
**TSerializable** 接口中提供了快捷方法 `write`，直接调用即可序列化为字符串
```java
SimpleData date = new SimpleData();
date.number = 999;
System.out.println(data.write());
```

## 反序列化（反向读取）
**TSerializable** 接口中提供了快捷方法 `read`，直接调用即可反向读取数据  
```
SimpleData date = new SimpleData();
data.read("{\"serializeObject\":{\"text\":\"123\",\"number\":\"100\",\"list\":\"[\\\"1.0\\\",\\\"2.0\\\",\\\"3.0\\\"]\"}}");
```
