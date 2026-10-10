package com.zaxxer.hikari_4_0_3

/**
 * 测试环境使用 database 模块 shadow 产物，字节码会引用重定位后的 HikariDataSource。
 * 生产环境由 TabooLib 运行时依赖提供，这里只在 test classpath 代理到原始 Hikari。
 */
class HikariDataSource(config: HikariConfig) : com.zaxxer.hikari.HikariDataSource(config)
