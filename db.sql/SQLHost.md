# SQLHost
用于 TabooLib 的数据库相关工具中作为数据库地址使用

## 创建

**从 ConfigurationSection 中读取数据库地址**
```java
public class Plugin extends Plugin {

    private SQLHost host;

    @Override
    public void onEnable() {
        host = new SQLHost(getConfig().getConfigurationSection("Database"), this);
    }
}
```
```yaml
# 任意节点
Database:
  # 地址
  host: localhost
  # 用户
  user: root
  # 端口
  port: 3306
  # 密码
  password: ''
  # 数据库
  database: test
```

**使用数据库信息直接创建**
```java
public class Plugin extends Plugin {

    private SQLHost host;

    @Override
    public void onEnable() {
        host = new SQLHost("localhost", "user", "3306", "", "test", this);
    }
}
```
