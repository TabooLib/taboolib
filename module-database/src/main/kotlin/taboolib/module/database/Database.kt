package taboolib.module.database

import taboolib.common.env.RuntimeDependency

@RuntimeDependency("com.zaxxer:HikariCP:4.0.3", test = "com.zaxxer.hikari.HikariDataSource")
object Database {

}