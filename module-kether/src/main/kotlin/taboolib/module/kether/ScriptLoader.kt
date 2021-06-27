package taboolib.module.kether

import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.loader.SimpleQuestLoader
import java.nio.charset.StandardCharsets
import java.util.*

object ScriptLoader {

    fun load(str: String, namespace: List<String> = emptyList()): Quest {
        return SimpleQuestLoader().load(ScriptService, "temp_${UUID.randomUUID()}", str.toByteArray(StandardCharsets.UTF_8), namespace)
    }

    fun load(str: List<String>, namespace: List<String> = emptyList()): Quest {
        return load(str.joinToString("\n"), namespace)
    }
}