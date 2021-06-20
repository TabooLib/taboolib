# 线程池

> 数据库线程池创建工具

## 作用

快速创建基于 HikariCP 的数据库（MySQL/SQLite）线程池

## 创建

```java
SQLHost host = ...
DataSource dataSource = DBSource.create(host);
```

> 当数据库连接失败时，该方法会抛出 `SQLException` 异常

## 关闭

```java
SQLHost host = ...
DataSource dataSource = DBSource.closeDataSource(conf);
```

> 创建 `IHost` 时如果没有设置 `AutoClose`，请勿忘记在插件卸载时关闭数据库连接，以兼容热重载。

## 配置

你可以在 `datasource.yml` 文件中线程池的各项设定

```yaml
# 默认连接池配置
DefaultSettings:
  DriverClassName: 'com.mysql.jdbc.Driver'
  # 自动提交从池中返回的连接
  AutoCommit: true
  # 池中维护的最小空闲连接数
  MinimumIdle: 1
  # 池中最大连接数，包括闲置和使用中的连接
  MaximumPoolSize: 10
  # 用来指定验证连接有效性的超时时间
  ValidationTimeout: 5000
  # 等待来自池的连接的最大毫秒数
  ConnectionTimeout: 30000
  # 一个连接idle状态的最大时长，超时则被释放
  IdleTimeout: 600000
  # 一个连接的生命时长，超时而且没被使用则被释放
  MaxLifetime: 1800000
  # 如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性
  ConnectionTestQuery: SELECT 1
  # 其他自定义配置
  DataSourceProperty: {}
#    cachePrepStmts: true
#    prepStmtCacheSize: 250
#    prepStmtCacheSqlLimit: 2048
#    useServerPrepStmts: true
#    useLocalSessionState: true
#    useLocalTransactionState: true
#    rewriteBatchedStatements: true
#    cacheResultSetMetadata: true
#    cacheServerConfiguration: true
#    elideSetAutoCommits: true
#    maintainTimeStats: false
```
