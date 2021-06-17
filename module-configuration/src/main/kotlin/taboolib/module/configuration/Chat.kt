package taboolib.module.configuration

import taboolib.library.configuration.MemorySection
import taboolib.module.chat.colored

fun MemorySection.getStringColored(node: String, def: String? = null) = getString(node, def)?.colored()

fun MemorySection.getStringListColored(node: String) = getStringList(node).colored()