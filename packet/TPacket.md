# TPacket
轻便的数据包操作工具  
不吹牛逼，效率高过 ProtocolLib

## 创建监听
```java
public class Plugin extends Plugin {

    @Override
    public void onEnable() {
        TPacketHandler.addListener(this, new TPacketListener() {
        
            @Override
            public boolean onSend(Player player, Object packet) {
                return true;
            }

            @Override
            public boolean onReceive(Player player, Object packet) {
                return true;
            }
        });
    }
}
```

## 示范：屏蔽关键字
```java
public class Plugin extends Plugin {

    @Override
    public void onEnable() {
        // 缓存变量
        SimpleReflection.saveField(PacketPlayOutChat.class);
        // 注册监听
        TPacketHandler.addListener(this, new TPacketListener() {
        
            @Override
            public boolean onSend(Player player, Object packet) {
                if (packet instanceof PacketPlayOutChat) {
                    // 反射获取信息内容
                    IChatBaseComponent c = (IChatBaseComponent) SimpleReflection.getFieldValue(PacketPlayOutChat.class, packet, "a");
                    // 如果有指定内容则取消发送（获取文本方法可能不准确不过大概是这个意思）
                    return !c.getText().contains("【脏话】");
                }
                return true;
            }
        });
    }
}
```
!> 搭配 SimpleVersionControl 一起食用效果更佳。
