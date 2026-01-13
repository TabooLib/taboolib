package taboolib.module.nms

import com.mojang.authlib.properties.PropertyMap
import net.minecraft.core.ClientAsset
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.resources.MinecraftKey
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEffect
import net.minecraft.world.ChestLock
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectList
import net.minecraft.world.entity.animal.*
import net.minecraft.world.entity.animal.axolotl.Axolotl
import net.minecraft.world.entity.animal.frog.FrogVariant
import net.minecraft.world.entity.animal.horse.EntityLlama
import net.minecraft.world.entity.animal.horse.HorseColor
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant
import net.minecraft.world.entity.animal.wolf.WolfVariant
import net.minecraft.world.entity.decoration.PaintingVariant
import net.minecraft.world.entity.npc.VillagerType
import net.minecraft.world.entity.variant.ModelAndTexture
import net.minecraft.world.entity.variant.SpawnPrioritySelectors
import net.minecraft.world.food.FoodInfo
import net.minecraft.world.item.*
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.PotionRegistry
import net.minecraft.world.item.component.*
import net.minecraft.world.item.consume_effects.*
import net.minecraft.world.item.enchantment.Enchantable
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.item.enchantment.Repairable
import net.minecraft.world.item.equipment.Equippable
import net.minecraft.world.item.equipment.trim.ArmorTrim
import net.minecraft.world.level.block.entity.BannerPatternLayers
import net.minecraft.world.level.block.entity.EnumBannerPatternType
import net.minecraft.world.level.block.entity.PotDecorations
import net.minecraft.world.level.saveddata.maps.MapId
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.platform.function.info
import java.util.*
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * TabooLib
 * taboolib.module.nms.ItemComponentImpl
 *
 * @author 晓劫
 * @since 2025/11/20 11:23
 */
class ItemComponentImpl : ItemComponent {

    override fun getTagData(nbtTag: Any): ItemTagData? {
        return when (nbtTag) {
            is IChatBaseComponent -> getIChatBaseComponent(nbtTag)
            is ItemLore -> getItemLore(nbtTag)
            is ItemEnchantments -> getItemEnchantments(nbtTag)
            is MinecraftKey -> getMinecraftKey(nbtTag)
            is FoodInfo -> getFoodInfo(nbtTag)
            is UseCooldown -> getUseCooldown(nbtTag)
            is TooltipDisplay -> getTooltipDisplay(nbtTag)
            is CustomData -> getCustomData(nbtTag)
            is MapId -> getMapId(nbtTag)
            is CustomModelData -> getCustomModelData(nbtTag)
            is EnumItemRarity -> getEnumItemRarity(nbtTag)
            is AdventureModePredicate -> getAdventureModePredicate(nbtTag)
            is ItemAttributeModifiers -> getItemAttributeModifiers(nbtTag)
            is Consumable -> getConsumable(nbtTag)
            is UseRemainder -> getUseRemainder(nbtTag)
            is DamageResistant -> getDamageResistant(nbtTag)
            is Tool -> getTool(nbtTag)
            is Weapon -> getWeapon(nbtTag)
            is Enchantable -> getEnchantable(nbtTag)
            is Equippable -> getEquippable(nbtTag)
            is Repairable -> getRepairable(nbtTag)
            is DeathProtection -> getDeathProtection(nbtTag)
            is BlocksAttacks -> getBlocksAttacks(nbtTag)
            is DyedItemColor -> getDyedItemColor(nbtTag)
            is MapItemColor -> getMapItemColor(nbtTag)
            is MapDecorations -> getMapDecorations(nbtTag)
            is Holder<*> -> parseHolder(nbtTag)
            is MapPostProcessing -> getMapPostProcessing(nbtTag)
            is ChargedProjectiles -> getChargedProjectiles(nbtTag)
            is BundleContents -> getBundleContents(nbtTag)
            is PotionContents -> getPotionContents(nbtTag)
            is SuspiciousStewEffects -> getSuspiciousStewEffects(nbtTag)
            is WritableBookContent -> getWritableBookContent(nbtTag)
            is WrittenBookContent -> getWrittenBookContent(nbtTag)
            is ArmorTrim -> getArmorTrim(nbtTag)
            is DebugStickState -> getDebugStickState(nbtTag)
            is InstrumentComponent -> getInstrumentComponent(nbtTag)
            is ProvidesTrimMaterial -> getProvidesTrimMaterial(nbtTag)
            is OminousBottleAmplifier -> getOminousBottleAmplifier(nbtTag)
            is JukeboxPlayable -> getJukeboxPlayable(nbtTag)
            is EnumBannerPatternType -> getEnumBannerPatternType(nbtTag)
            is LodestoneTracker -> getLodestoneTracker(nbtTag)
            is FireworkExplosion -> getFireworkExplosion(nbtTag)
            is Fireworks -> getFireworks(nbtTag)
            is ResolvableProfile -> getResolvableProfile(nbtTag)
            is BannerPatternLayers -> getBannerPatternLayers(nbtTag)
            is EnumColor -> getEnumColor(nbtTag)
            is PotDecorations -> getPotDecorations(nbtTag)
            is ItemContainerContents -> getItemContainerContents(nbtTag)
            is BlockItemStateProperties -> getBlockItemStateProperties(nbtTag)
            is Bees -> getBees(nbtTag)
            is ChestLock -> getChestLock(nbtTag)
            is SeededContainerLoot -> getSeededContainerLoot(nbtTag)
            is EntityTropicalFish.Variant -> getEntityTropicalFishVariant(nbtTag)
            is ResourceKey<*> -> {
                val location = "location" to getMinecraftKey(nbtTag.location())
                val registry = "registry" to getMinecraftKey(nbtTag.registry())
                itemTag(location, registry)
            }

            is EntityFox.Type -> getEntityFoxType(nbtTag)
            is EntitySalmon.Variant -> getEntitySalmonVariant(nbtTag)
            is EntityParrot.Variant -> getEntityParrotVariant(nbtTag)
            is EntityMushroomCow.Type -> getEntityMushroomCowType(nbtTag)
            is EntityRabbit.Variant -> getEntityRabbitVariant(nbtTag)
            is EitherHolder<*> -> TODO("EitherHolder<ChickenVariant> 暂未实现")
            is HorseColor -> getHorseColor(nbtTag)
            is EntityLlama.Variant -> getEntityLlamaVariant(nbtTag)
            is Axolotl.Variant -> getAxolotlVariant(nbtTag)
            is DataComponentType<*> -> ItemTagData("null")
            else -> null
        }
    }

    fun getAxolotlVariant(value: Axolotl.Variant): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        return itemTag(name, id)
    }

    fun getHorseColor(value: HorseColor): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        return itemTag(name, id)
    }

    fun getEntityRabbitVariant(value: EntityRabbit.Variant): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id()
        return itemTag(name, id)
    }

    fun getEntityMushroomCowType(value: EntityMushroomCow.Type): ItemTagData {
        val name = "name" to value.name
        return itemTag(name)
    }

    fun getEntitySalmonVariant(value: EntitySalmon.Variant): ItemTagData {
        val name = "name" to value.name
        return itemTag(name)
    }

    fun getEntityFoxType(value: EntityFox.Type): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        return itemTag(name, id)
    }

    fun getEntityParrotVariant(value: EntityParrot.Variant): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        return itemTag(name, id)
    }

    fun getEntityLlamaVariant(value: EntityLlama.Variant): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        return itemTag(name, id)
    }

    fun getSuspiciousStewEffects(value: SuspiciousStewEffects): ItemTagData {
        return ItemTagList(value.effects.map {
            val effect = "effect" to parseHolder(it.effect)
            val duration = "duration" to it.duration
            itemTag(effect, duration)
        })
    }

    fun getWritableBookContent(value: WritableBookContent): ItemTagData {
        return ItemTagList(value.pages.map {
            val raw = "raw" to it.raw
            val filtered = "filtered" to it.filtered.get()
            itemTag(raw, filtered)
        })
    }

    fun getWrittenBookContent(value: WrittenBookContent): ItemTagData {
        val author = "author" to value.author
        val generation = "generation" to value.generation
        val resolved = "resolved" to value.resolved
        val title = "title" to value.title
        val pages = "pages" to ItemTagList(value.pages.map {
            val raw = "raw" to it.raw
            val filtered = "filtered" to it.filtered.get()
            itemTag(raw, filtered)
        })
        return itemTag(author, generation, resolved, title, pages)
    }

    fun getArmorTrim(value: ArmorTrim): ItemTagData {
        val pattern = value.pattern.value().let {
            val decal = "decal" to it.decal
            val assetId = "assetId" to it.assetId.toString()
            val description = "description" to it.description.string
            "pattern" to itemTag(decal, assetId, description)
        }

        val material = value.material.value()
        val descriptionMaterial = "description" to material.description.string
        val suffix = "suffix" to material.assets.base.suffix
        val assets = "assest" to ItemTagList(material.assets.overrides.map {
            val location = "location" to it.key.location().toString()
            val suffixAssets = "suffix" to it.value.suffix
            itemTag(location, suffixAssets)
        })
        return itemTag(pattern, descriptionMaterial, suffix, assets)
    }

    fun getDebugStickState(value: DebugStickState): ItemTagData {
        TODO("DebugStickState 等待处理")
    }

    fun getInstrumentComponent(value: InstrumentComponent): ItemTagData {
        TODO("InstrumentComponent 暂未实现")
    }

    fun getProvidesTrimMaterial(value: ProvidesTrimMaterial): ItemTagData {
        TODO("ProvidesTrimMaterial 暂未实现")
    }

    fun getOminousBottleAmplifier(value: OminousBottleAmplifier): ItemTagData {
        return ItemTagData(value.value)
    }

    fun getJukeboxPlayable(value: JukeboxPlayable): ItemTagData {
        TODO("JukeboxPlayable 暂未实现")
    }

    fun getEnumBannerPatternType(value: EnumBannerPatternType): ItemTagData {
        val assetId = "assetId" to getMinecraftKey(value.assetId)
        val translationKey = "translationKey" to value.translationKey
        return itemTag(assetId, translationKey)
    }

    fun getLodestoneTracker(value: LodestoneTracker): ItemTagData {
        val target = value.target.get()
        val pos = "pos" to target.pos.toShortString()
        val tracked = "tracked" to value.tracked
        return itemTag(pos, tracked)
    }

    fun getFireworkExplosion(value: FireworkExplosion): ItemTagData {
        val colors = "colors" to ItemTagList(value.colors.map { ItemTagData(it) })
        val fadeColors = "fadeColors" to ItemTagList(value.fadeColors.map { ItemTagData(it) })
        val hasTrail = "hasTrail" to value.hasTrail
        val hasTwinkle = "hasTwinkle" to value.hasTwinkle
        val shape = "shape.name" to value.shape.name
        return itemTag(colors, fadeColors, hasTrail, hasTwinkle, shape)
    }

    fun getFireworks(value: Fireworks): ItemTagData {
        val explosions = "explosions" to ItemTagList(value.explosions.map { getFireworkExplosion(it) })
        val flightDuration = "flightDuration" to value.flightDuration
        return itemTag(explosions, flightDuration)
    }

    fun getResolvableProfile(value: ResolvableProfile): ItemTagData {
        val id = "id" to value.id.get().toString()
        val name = "name" to value.name.get()
        val isResolved = "isResolved" to value.isResolved

        val game = value.gameProfile
        val profileId = "id" to game.id.toString()
        val profileName = "name" to game.name
        val profile = "properties" to getProperties(game.properties)
        val gameProfile = "gameProfile" to itemTag(profileId, profileName, profile)

        val properties = "properties" to getProperties(value.properties)
        return itemTag(id, name, isResolved, gameProfile, properties)
    }

    fun getProperties(value: PropertyMap): ItemTagData {
        return ItemTagList(value.entries().map {
            val n = "name" to it.value.name
            val v = "value" to it.value.value
            val signature = "signature" to it.value.signature.toString()
            itemTag(it.key to itemTag(n, v, signature))
        })
    }

    fun getBannerPatternLayers(value: BannerPatternLayers): ItemTagData {
        return ItemTagList(value.layers.map {
            val pattern = it.pattern.value()
            val assetId = "assetId" to pattern.assetId.toString()
            val translationKey = "translationKey" to pattern.translationKey
            val color = "color" to it.color.name
            itemTag(assetId, translationKey, color)
        })
    }

    fun getEnumColor(value: EnumColor): ItemTagData {
        val name = "name" to value.name
        val id = "id" to value.id
        val mapColor = "mapColor" to value.mapColor
        val textColor = "textColor" to value.textColor
        val fireworkColor = "fireworkColor" to value.fireworkColor
        val textureDiffuseColor = "textureDiffuseColor" to value.textureDiffuseColor
        return itemTag(name, id, mapColor, textColor, fireworkColor, textureDiffuseColor)
    }

    fun getPotDecorations(value: PotDecorations): ItemTagData {
        TODO("PotDecorations 暂未支持")
    }

    fun getItemContainerContents(value: ItemContainerContents): ItemTagData {
        TODO("ItemContainerContents 没有可用的参数")
    }

    fun getBlockItemStateProperties(value: BlockItemStateProperties): ItemTagData {
        return itemTag(*value.properties.map { it.key to it.value }.toTypedArray())
    }

    fun getBees(value: Bees): ItemTagData {
        return ItemTagList(value.bees.map {
            val entityData = "entityData" to getCustomData(it.entityData)
            val ticksInHive = "ticksInHive" to it.ticksInHive
            val minTicksInHive = "minTicksInHive" to it.minTicksInHive
            itemTag(entityData, ticksInHive, minTicksInHive)
        })
    }

    fun getChestLock(value: ChestLock): ItemTagData {
        value.predicate.components
        value.predicate.count.max
        TODO("ChestLock 暂未支持")
    }

    fun getSeededContainerLoot(value: SeededContainerLoot): ItemTagData {
        val seed = "seed" to value.seed
        val location = "location" to value.lootTable.location()
        val registry = "registry" to value.lootTable.registry()
        return itemTag(seed, "lootTable" to itemTag(location, registry))
    }

    fun getEntityTropicalFishVariant(value: EntityTropicalFish.Variant): ItemTagData {
        val name = "name" to value.name
        val base = "base" to value.base().name
        val displayName = "displayName" to value.displayName()
        val packedId = "packedId" to value.packedId
        val serializedName = "serializedName" to value.serializedName
        return itemTag(name, base, displayName, packedId, serializedName)
    }

    fun getPotionContents(value: PotionContents): ItemTagData {
        val color = "color" to value.color
        val potion = "potion" to parseHolder(value.potion.get())
        val allEffects = "allEffects" to ItemTagList(value.allEffects.map { getMobEffect(it) })

        val customName = "customName" to value.customName.get()
        val customColor = "customColor" to value.customColor.get()
        val customEffects = "customEffects" to ItemTagList(value.customEffects.map { getMobEffect(it) })
        return itemTag(color, potion, allEffects, customName, customColor, customEffects)
    }

    fun getBundleContents(value: BundleContents): ItemTagData {
        // TODO("items列表还不知道怎么处理")
        val isEmpty = "isEmpty" to value.isEmpty
        val selectedItem = "selectedItem" to value.selectedItem
        val numberOfItemsToShow = "numberOfItemsToShow" to value.numberOfItemsToShow
        val weight = "weight" to value.weight().let {
            val denominator = "denominator" to it.denominator
            val numberator = "numerator" to it.numerator
            val properWhole = "properWhole" to it.properWhole
            val properNumberator = "properNumerator" to it.properNumerator
            itemTag(denominator, numberator, properWhole, properNumberator)
        }
        val size = "size" to value.size()
        val hasSelectedItem = "hasSelectedItem" to value.hasSelectedItem()
        return itemTag(isEmpty, selectedItem, numberOfItemsToShow, weight, size, hasSelectedItem)
    }

    fun getChargedProjectiles(value: ChargedProjectiles): ItemTagData {
        // TODO("items列表还不知道怎么处理")
        return ItemTagData(value.isEmpty)
    }

    fun getMapPostProcessing(value: MapPostProcessing): ItemTagData {
        return ItemTagData(value.name)
    }

    fun getMapDecorations(value: MapDecorations): ItemTagData {
        return ItemTagList(value.decorations.map {
            val x = "x" to it.value.x
            val z = "z" to it.value.z
            val rotation = "rotation" to it.value.rotation
            val type = it.value.type.value()
            val assetId = "assetId" to type.assetId.toString()
            val mapColor = "mapColor" to type.mapColor
            val types = "type" to itemTag(assetId, mapColor)

            itemTag(it.key to itemTag(x, z, rotation, types))
        })
    }

    fun getMapItemColor(value: MapItemColor): ItemTagData {
        return ItemTagData(value.rgb)
    }

    fun getDyedItemColor(value: DyedItemColor): ItemTagData {
        return ItemTagData(value.rgb)
    }

    fun getBlocksAttacks(value: BlocksAttacks): ItemTagData {
        val soundEffect = "soundEffect" to getSoundEffect(value.blockSound.get().value())
        val blockDelaySeconds = "blockDelaySeconds" to value.blockDelaySeconds
        val damageReductions = "damageReductions" to ItemTagList(value.damageReductions.map {
            val base = "base" to it.base
            val factor = "factor" to it.factor
            val horizontalBlockingAngle = "horizontalBlockingAngle" to it.horizontalBlockingAngle
            val type = "type" to ItemTagList(it.type.get().map {
                val values = it.value()
                val msgId = "msgId" to values.msgId
                val deathMessageType = "deathMessageType" to values.deathMessageType.name
                val effects = "effects" to values.effects.name
                val scaling = "scaling" to values.scaling.name
                val exhaustion = "exhaustion" to values.exhaustion
                itemTag(msgId, deathMessageType, effects, scaling, exhaustion)
            })
            itemTag(base, factor, horizontalBlockingAngle, type)
        })
        return itemTag(soundEffect, blockDelaySeconds, damageReductions)
    }

    fun getDeathProtection(value: DeathProtection): ItemTagData {
        val deaths = value.deathEffects.mapNotNull {
            when (it.type) {
                ConsumeEffect.a.APPLY_EFFECTS -> ItemTagList((it as ApplyStatusEffectsConsumeEffect).effects.map { getMobEffect(it) })

                ConsumeEffect.a.REMOVE_EFFECTS -> ItemTagList((it as RemoveStatusEffectsConsumeEffect).effects.map { parseHolder(it) })

                ConsumeEffect.a.PLAY_SOUND -> {
                    val sound = (it as PlaySoundConsumeEffect).sound.value()
                    val soundLocation = "location" to sound.location.toString()
                    val soundFixedRange = "fixedRange" to sound.fixedRange.get()
                    itemTag(soundFixedRange, soundLocation)
                }

                ConsumeEffect.a.TELEPORT_RANDOMLY -> ItemTagData((it as TeleportRandomlyConsumeEffect).diameter)

                else -> null
            }
        }
        return ItemTagList(deaths)
    }

    fun getRepairable(value: Repairable): ItemTagData {
        return ItemTagList(value.items.map { ItemTagData(it.value().name.string) })
    }

    fun getEquippable(value: Equippable): ItemTagData {
        val assetId = "assetId" to value.assetId.get().location().toString()

        val slotType = "name" to value.slot.type.name
        val slotName = "slot" to value.slot.name
        val index = "index" to value.slot.index
        val id = "id" to value.slot.id
        val slot = "slot" to itemTag(slotType, slotName, index, id)

        val swappable = "swappable" to value.swappable.toString()
        val dispensable = "dispensable" to value.dispensable.toString()
        val canBeSheared = "canBeSheared" to value.canBeSheared.toString()
        val damageOnHurt = "damageOnHurt" to value.damageOnHurt.toString()
        val equipOnInteract = "equipOnInteract" to value.equipOnInteract.toString()

        val soundLocation = "location" to value.equipSound.value().location.toString()
        val soundRange = "fixedRange" to value.equipSound.value().fixedRange.getOrDefault(0F)
        val equipSound = "equipSound" to itemTag(soundLocation, soundRange)
        return itemTag(assetId, slot, swappable, dispensable, canBeSheared, damageOnHurt, equipOnInteract, equipSound)
    }

    fun getEnchantable(value: Enchantable): ItemTagData {
        return itemTag("value" to value.value)
    }

    fun getWeapon(value: Weapon): ItemTagData {
        val itemDamagePerAttack = "itemDamagePerAttack" to value.itemDamagePerAttack
        val disableBlockingForSeconds = "disableBlockingForSeconds" to value.disableBlockingForSeconds
        return itemTag(itemDamagePerAttack, disableBlockingForSeconds)
    }

    fun getTool(value: Tool): ItemTagData {
        val rules = "rules" to itemTag("speeds" to ItemTagList(value.rules.map { ItemTagData(it.speed.getOrDefault(-1f)) }))
        val correctForDrops = "correctForDrops" to ItemTagList(value.rules.map { ItemTagData(it.correctForDrops.getOrDefault(false)) })
        val damagePerBlock = "damagePerBlock" to value.damagePerBlock
        val defaultMiningSpeed = "defaultMiningSpeed" to value.defaultMiningSpeed
        val canDestroyBlocksInCreative = "canDestroyBlocksInCreative" to value.canDestroyBlocksInCreative.toString()
        return itemTag(rules, correctForDrops, damagePerBlock, defaultMiningSpeed, canDestroyBlocksInCreative)
    }

    fun getDamageResistant(value: DamageResistant): ItemTagData {
        val location = "location" to value.types.location.toString()
        val typesRegistry = "location" to value.types.registry.location().toString()
        val typesLocation = "location" to value.types.location.toString()
        return itemTag(location, typesLocation, typesRegistry)
    }

    fun getUseRemainder(value: UseRemainder): ItemTagData {
        return ItemTag.fromJson(NMSItemTag.let {
            it.instance.toMinecraftJson(it.asBukkitCopy(value.convertInto))
        })
    }

    fun getConsumable(value: Consumable): ItemTagData {
        val soundRange = "fixedRange" to (value.sound.value().fixedRange.getOrNull() ?: 0F)
        val soundLocation = "location" to value.sound.value().location.toString()
        val name = "registeredName" to value.sound.registeredName
        return itemTag(soundRange, soundLocation, name)
    }

    fun getItemAttributeModifiers(value: ItemAttributeModifiers): ItemTagData {
        val modifiers = try {
            value.modifiers
        } catch (_: NoSuchMethodError) {
            value.invokeMethod<List<ItemAttributeModifiers.c>>("modifiers")!!
        }

        val attribute = "attribute" to ItemTagList(modifiers.map {
            val amount = "amount" to it.modifier.amount
            val defaultValue = "defaultValue" to it.attribute.value().defaultValue
            val descriptionId = "id" to it.attribute.value().descriptionId.substringAfterLast(".")
            itemTag(descriptionId, amount, defaultValue)
        })
        return itemTag(attribute)
    }

    fun getAdventureModePredicate(value: AdventureModePredicate): ItemTagData {
        val tooltip = "tooltip" to ItemTagList(value.invokeMethod<List<IChatBaseComponent>>("tooltip")?.map { getIChatBaseComponent(it) } ?: listOf())
        return itemTag(tooltip)
    }

    fun getEnumItemRarity(value: EnumItemRarity): ItemTagData {
        val name = "name" to value.name
        return itemTag(name)
    }

    fun getCustomModelData(value: CustomModelData): ItemTagData {
        val colors = "colors" to ItemTagList(value.colors.map { itemTagToBukkitCopy(it) })
        val floats = "floats" to ItemTagList(value.floats.map { itemTagToBukkitCopy(it) })
        val flags = "flags" to ItemTagList(value.flags.map { itemTagToBukkitCopy(it) })
        val strings = "string" to ItemTagList(value.strings.map { itemTagToBukkitCopy(it) })
        return itemTag(colors, floats, flags, strings)
    }

    fun getMapId(value: MapId): ItemTagData {
        return ItemTagData(value.key())
    }

    fun getCustomData(value: CustomData): ItemTagData {
        return itemTagToBukkitCopy(value.copyTag())
    }

    fun getTooltipDisplay(value: TooltipDisplay): ItemTagData {
        val hideTool = try {
            value.hideTooltip()
        } catch (_: NoSuchMethodError) {
            value.getProperty<Boolean>("hideTooltip")!!
        }

//        val components = try {
//            value.hiddenComponents
//        } catch (_: NoSuchMethodError) {
//            value.getProperty<Set<DataComponentType<*>>>("hiddenComponents")!!
//        }
        val hideTooltip = "hideTooltip" to hideTool
//        val hiddenComponents = "hiddenComponents" to ItemTagList(components.map { ItemTagData(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(it).toString()) })
        return itemTag(hideTooltip)
    }

    fun getUseCooldown(value: UseCooldown): ItemTagData {
        val s = "seconds" to value.seconds
        val group = "group" to value.cooldownGroup.toString()
        return itemTag(s, group)
    }

    fun getFoodInfo(value: FoodInfo): ItemTagData {
        val nutrition = "nutrition" to value.nutrition
        val canAlwaysEat = "canAlwaysEat" to value.canAlwaysEat().toString()
        val saturation = "saturation" to value.saturation
        return itemTag(nutrition, canAlwaysEat, saturation)
    }

    fun getMinecraftKey(value: MinecraftKey): ItemTagData {
        return ItemTagData(value.toString())
    }

    fun getItemEnchantments(value: ItemEnchantments): ItemTagData {
        return try {
            value.keySet()
        } catch (_: NoSuchMethodError) {
            value.invokeMethod<Set<Holder<Enchantment>>>("keySet")!!
        }.associateWith { it.value() }.let {
            ItemTag(it.map { it.key.registeredName to itemTagToBukkitCopy(it.value.weight) }.toMap())
        }
    }

    fun getItemLore(value: ItemLore): ItemTagData {
        val lines = try {
            value.lines
        } catch (_: NoSuchMethodError) {
            value.invokeMethod<List<Any>>("lines")!!
        }.map { itemTagToBukkitCopy(it) }

        return ItemTagList(lines)
    }

    fun getIChatBaseComponent(value: IChatBaseComponent): ItemTagData {
        return ItemTagData(value.string)
    }

    fun parseHolder(holder: Holder<*>): ItemTagData {
        val value = try {
            holder.value()
        } catch (_: NoSuchMethodError) {
            holder.invokeMethod<Any>("value")!!
        }
        return when (value) {
            is SoundEffect -> getSoundEffect(value)
            is MobEffectList -> getMobEffectList(value)
            is PotionRegistry -> getPotionRegistry(value)
            is VillagerType -> getVillagerType(value)
            is WolfVariant -> getWolfVariant(value)
            is WolfSoundVariant -> getWolfSoundVariant(value)
            is PigVariant -> getPigVariant(value)
            is CowVariant -> getCowVariant(value)
            is CatVariant -> getCatVariant(value)
            is PaintingVariant -> getPaintingVariant(value)
            is FrogVariant -> getFrogVariant(value)

            else -> error("Unsupported holder type: ${value.javaClass}")
        }
    }


    fun getWolfSoundVariant(value: WolfSoundVariant): ItemTagData {
        val hurtSound = "hurtSound" to parseHolder(value.hurtSound)
        val deathSound = "deathSound" to parseHolder(value.deathSound)
        val pantSound = "pantSound" to parseHolder(value.pantSound)
        val growlSound = "growlSound" to parseHolder(value.growlSound)
        val whineSound = "whineSound" to parseHolder(value.whineSound)
        val ambientSound = "ambientSound" to parseHolder(value.ambientSound)
        return itemTag(hurtSound, deathSound, pantSound, growlSound, whineSound, ambientSound)
    }

    fun getPaintingVariant(value: PaintingVariant): ItemTagData {
        val author = "author" to getIChatBaseComponent(value.author.get())
        val title = "title" to getIChatBaseComponent(value.title.get())
        val assetId = "assetId" to getMinecraftKey(value.assetId)
        val height = "height" to value.height
        val width = "width" to value.width
        val area = "area" to value.area()
        return itemTag(author, title, assetId, height, width, area)
    }

    fun getCatVariant(value: CatVariant): ItemTagData {
        val assetInfo = "assetInfo" to parseClientAsset(value.assetInfo)
        val spawnConditions = "spawnConditions" to parseSpawnPrioritySelectors(value.spawnConditions)
        return itemTag(assetInfo, spawnConditions)
    }

    fun getCowVariant(value: CowVariant): ItemTagData {
        val spawnConditions = "spawnConditions" to parseSpawnPrioritySelectors(value.spawnConditions)
        val modelAndTexture = "modelAndTexture" to parseModel(value.modelAndTexture)
        return itemTag(spawnConditions, modelAndTexture)
    }

    fun getPigVariant(value: PigVariant): ItemTagData {
        val spawnConditions = "spawnConditions" to parseSpawnPrioritySelectors(value.spawnConditions)
        val modelAndTexture = "modelAndTexture" to parseModel(value.modelAndTexture)
        return itemTag(spawnConditions, modelAndTexture)
    }

    fun getWolfVariant(value: WolfVariant): ItemTagData {
        val info = value.assetInfo
        val tame = "tame" to parseClientAsset(info.tame)
        val wild = "wild" to parseClientAsset(info.wild)
        val angry = "angry" to parseClientAsset(info.angry)
        val assetInfo = "assetInfo" to itemTag(tame, wild, angry)
        val spawnConditions = "spawnConditions" to parseSpawnPrioritySelectors(value.spawnConditions)
        return itemTag(assetInfo, spawnConditions)
    }

    fun getVillagerType(value: VillagerType): ItemTagData {
        TODO("VillagerType 没有可用的内容)")
    }

    fun getFrogVariant(value: FrogVariant): ItemTagData {
        val assetInfo = "assetInfo" to parseClientAsset(value.assetInfo)
        val spawnConditions = "spawnConditions" to parseSpawnPrioritySelectors(value.spawnConditions)
        return itemTag(assetInfo, spawnConditions)
    }

    fun getPotionRegistry(value: PotionRegistry): ItemTagData {
        val effects = "effects" to ItemTagList(value.effects.map { getMobEffect(it) })
        val name = "name" to value.name()
        return itemTag(effects, name)
    }

    fun getMobEffect(value: MobEffect): ItemTagData {
        val amplifier = "amplifier" to value.amplifier
        val duration = "duration" to value.duration
        val descriptionId = "descriptionId" to value.descriptionId
        val effects = "effects" to parseHolder(value.effect)
        return itemTag(amplifier, duration, descriptionId, effects)
    }

    fun getMobEffectList(value: MobEffectList): ItemTagData {
        val color = "color" to value.color
        val category = "category" to value.category.let {
            val name = "name" to it.name
            val tooltipFormatting = "tooltipFormatting" to it.tooltipFormatting.name
            itemTag(name, tooltipFormatting)
        }
        val descriptionId = "descriptionId" to value.descriptionId
        val displayName = "displayName" to getIChatBaseComponent(value.displayName)
        val blendInDurationTicks = "blendInDurationTicks" to value.blendInDurationTicks
        val blendOutAdvanceTicks = "blendOutAdvanceTicks" to value.blendOutAdvanceTicks
        val blendOutDurationTicks = "blendOutDurationTicks" to value.blendOutDurationTicks
        val isBeneficial = "isBeneficial" to value.isBeneficial
        val isInstantenous = "isInstantenous" to value.isInstantenous

        return itemTag(
            color,
            category,
            descriptionId,
            displayName,
            blendOutDurationTicks,
            blendOutAdvanceTicks,
            blendInDurationTicks,
            isBeneficial,
            isInstantenous
        )
    }

    fun getSoundEffect(value: SoundEffect): ItemTagData {
        val loca = try {
            value.location
        } catch (_: NoSuchMethodError) {
            value.invokeMethod<MinecraftKey>("location")!!
        }
        val range = try {
            value.fixedRange
        } catch (_: NoSuchMethodError) {
            value.invokeMethod<Optional<Float>>("fixedRange")!!
        }.getOrDefault(0F)
        val location = "location" to loca.toString()
        val fixed = "fixedRange" to range
        return itemTag(location, fixed)
    }

    private fun parseModel(value: ModelAndTexture<*>): ItemTagData {
        val model = "name" to when (val model = value.model) {
            is PigVariant.a -> {
                model.name
            }

            is CowVariant.a -> {
                model.name
            }

            else -> "null"
        }
        val asset = "asset" to parseClientAsset(value.asset)
        return itemTag(model, asset)
    }

    private fun parseClientAsset(value: ClientAsset): ItemTagData {
        val id = "id" to getMinecraftKey(value.id)
        val texturePath = "texturePath" to getMinecraftKey(value.texturePath)
        return itemTag(id, texturePath)
    }

    private fun parseSpawnPrioritySelectors(value: SpawnPrioritySelectors): ItemTagData {
        val selectors = "selectors" to ItemTagList(value.selectors.map {
            val priority = "priority" to it.priority
            itemTag(priority)
        })
        return itemTag(selectors)
    }

    private fun itemTag(vararg pairs: Pair<String, Any>): ItemTagData {
        return ItemTag(pairs.associate { it.first to ((it.second as? ItemTagData) ?: ItemTagData.toNBT(it.second)) })
    }
}