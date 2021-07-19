package taboolib.common5

import taboolib.common.env.RuntimeDependency

@RuntimeDependency("!com.google.guava:guava:21.0", test = "!com.google.common.base.Optional")
object Common5