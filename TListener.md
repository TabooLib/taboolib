# TListener
懒癌第一步，省略注册方法  
当插件载入时 TabooLib 会为你 **自动注册** 监听器

## 使用环境
在我为我的服务器开发核心插件时，会出现这么一个问题。  
监听器很多，非常多，我又不想给他们写到一起，那样会很乱。  
分开写又要一条一条注册，太麻烦了！

## 基本用法
```java
@TListener
public class ListenerPlayerCommand implements Listener {

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equals("/test")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("Successfully.");
        }
    }
}
```

## 特殊参数
```java
@TListener(
    // 监听器注册时调用类中同名无参方法
    register = "register",
    // 监听器注销时...
    cancel = "cancel",
    // 监听器注册时判断类中同名方法
    condition = "check",
    // 如果依赖插件不存在则不会实例化该类
    depend = "TabooLib"
)
public class ListenerPlayerCommand implements Listener {
    
    public void register() {
        Bukkit.broadcastMessage("监听器已注册");
    }
    
    public void cancel() {
        Bukkit.broadcastMessage("监听器已注销");
    }
    
    /**
     * 该方法返回值类型必须为 boolean
     */
    public boolean check() {
        return true;
    }
}
```

## 易错问题
+ 为什么我添加 @TListener 注解了却没有反应？
  + 检查你的插件是否依赖于 TabooLib（depend: TabooLib in plugin.yml）
  + TabooLib 只会扫描依赖于它的插件
  
+ 为什么我的监听器注册时报错了？
  + 请保留无参构造器
  + 否则 TabooLib 将无法正常实例化该类
