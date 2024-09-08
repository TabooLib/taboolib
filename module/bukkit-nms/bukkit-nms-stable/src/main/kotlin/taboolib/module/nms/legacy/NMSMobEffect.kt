package taboolib.module.nms.legacy

import org.bukkit.potion.PotionEffectType
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.MinecraftLanguage
import taboolib.module.nms.MinecraftLanguage.LanguageKey.Type
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy

/**
 * 用于 1.13 版本及以下使用的
 */
abstract class NMSPotionEffect {

    /** 获取药水效果的语言文件节点，例如 `effect.minecraft.regeneration` */
    abstract fun getLanguageKey(potionEffectType: PotionEffectType): MinecraftLanguage.LanguageKey

    companion object {

        val instance by unsafeLazy { nmsProxy<NMSPotionEffect>() }
    }
}

// region NMSPotionEffectImpl
class NMSPotionEffectImpl : NMSPotionEffect() {

    override fun getLanguageKey(potionEffectType: PotionEffectType): MinecraftLanguage.LanguageKey {
        // 1.13+ 开始使用 SystemUtils.a("effect", IRegistry.MOB_EFFECT.getKey(this)) 获取 key
        val descriptionId = if (MinecraftVersion.isIn(MinecraftVersion.V1_13..MinecraftVersion.V1_16)) {
            net.minecraft.server.v1_13_R2.MobEffectList.fromId(potionEffectType.id)!!.c()
        }
        // 1.13- 方法相对原始
        else if (MinecraftVersion.isIn(MinecraftVersion.V1_9..MinecraftVersion.V1_12)) {
            net.minecraft.server.v1_12_R1.MobEffectList.fromId(potionEffectType.id)!!.a()
        }
        // 1.8
        else {
            net.minecraft.server.v1_8_R3.MobEffectList.byId[potionEffectType.id].a()
        }
        return MinecraftLanguage.LanguageKey(Type.NORMAL, descriptionId!!)
    }
}
// endregion