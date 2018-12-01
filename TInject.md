#TInject
懒癌第四步，省略变量赋值  

## 使用环境
人类的本质是... （自动化接口，能省则省就对了）

## 可用类型
```java
public class Plugin extends JavaPlugin {

    @TInject
    static Plugin inst;
    
    @TInject
    static TLogger logger;
    
    @TInject("prefix")
    static TLogger logger1;
    
    @TInject("filename")
    static TConfiguration conf;
    
    @TInject
    static SimpleCommandBuilder command = SimpleCommandBuilder.create("cmd", inst)
        .execute((sender, args) -> {
            return true;
        });
        
    @TInject
    static TPacketListener listener = new TPacketListener() {

        @Override
        public boolean onSend(Player player, Object packet) {
            return true;
        }

        @Override
        public boolean onReceive(Player player, Object packet) {
            return true;
        }
    };
}
```
!> 该注解只能用于静态类型
