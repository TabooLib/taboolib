# SQLTable
用于 TabooLib 的数据库相关工具中作为数据库表使用  
阅读前请确保您有相关的数据库使用经验

# 构造方法
**创建空的 SQLTable**
```java
public class Plugin extends Plugin {

    private SQLTable table;

    @Override
    public void onEnable() {
        table = new SQLTable("table_name");
    }
}
```

**创建含有 SQLColumn 数据的 SQLTable**
```java
public class Plugin extends Plugin {

    private SQLTable table;

    @Override
    public void onEnable() {
        table = new SQLTable("table_name", SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "username"), new SQLColumn(SQLColumnType.TEXT, "data"));
    }
}
```
!> 如果你不需要在数据库中创建表，则可以直接创建空的 SQLTable。（如修改其他插件的数据）

## 创建表（SQL）

SQLTable 可以根据添加的列生成数据库创建命令
```java
public class Example extends Plugin {

    private SQLHost host;
    private SQLTable table;
    private DataSource dataSource;

    @Override
    public void onEnable() {
        host = new SQLHost("localhost", "user", "3306", "", "test", this);
        table = new SQLTable("table_name", SQLColumn.PRIMARY_KEY_ID, new SQLColumn(SQLColumnType.TEXT, "username"), new SQLColumn(SQLColumnType.TEXT, "data"));
        try {
            dataSource = HikariHandler.createDataSource(host);
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        // 创建更新任务
        table.executeUpdate(table.createQuery())
                // 设置连接池
                .dataSource(dataSource)
                // 运行
                .run();
    }
}
```

## 插入数据

```
+-----+----------+---------------------------------------------+
| id  | username | data                                        |
+-----+----------+---------------------------------------------+
|   1 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
|   2 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
|   3 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
|   4 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
|   5 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
|   6 | BlackSKY | Flight [Check 1]. [Ping 0 ms] [TPS *20.0].  |
+-----+----------+---------------------------------------------+
```
```sql
insert into table_name values(null, 'Steve', 'Flight')
```
```java
public class Example extends Plugin {

    private SQLHost host;
    private SQLTable table;
    private DataSource dataSource;

    @Override
    public void onEnable() {
        // ...
    }
    
    public void insertData() {
        // 创建更新任务
        table.executeInsert("null, ?, ?")
                // 设置连接池
                .dataSource(dataSource)
                // 赋值
                .statement(s -> {
                    // 第一个问号对应的列为文本类型
                    s.setString(1, "Steve");
                    // 第二个问号对应的列为文本类型
                    s.setString(2, "Flight");
                })
                // 运行
                .run();
    }
}
```

写他妈的棒棒锤，不解释了。
