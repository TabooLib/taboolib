package me.skymc.taboolib.sound;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundUtils {

    public static void sound(Location paramLocation, String paramString, float paramFloat1, float paramFloat2) {
        String str = getModifiedSound(paramString);
        try {
            paramLocation.getWorld().playSound(paramLocation, Sound.valueOf(str), paramFloat1, paramFloat2);
        } catch (Exception localException) {
            MsgUtils.send("§4Bug with " + paramString + ". No such sound found. Please report it to the plugin creator :)");
        }
    }

    static String getModifiedSound(String str) {
        if (TabooLib.getVerint() < 10900) {
            str = str.replace("BLOCK_FIRE_EXTINGUISH", "FIZZ");
            str = str.replace("BLOCK_NOTE_HAT", "NOTE_STICKS");
            str = str.replace("ENTITY_SHEEP_DEATH", "SHEEP_IDLE");
            str = str.replace("ENTITY_LLAMA_ANGRY", "HORSE_HIT");
            str = str.replace("BLOCK_BREWING_STAND_BREW", "CREEPER_HISS");
            str = str.replace("ENTITY_SHULKER_TELEPORT", "ENDERMAN_TELEPORT");
            str = str.replace("ENTITY_ZOMBIE_ATTACK_IRON_DOOR", "ZOMBIE_METAL");
            str = str.replace("BLOCK_GRAVEL_BREAK", "DIG_GRAVEL");
            str = str.replace("BLOCK_SNOW_BREAK", "DIG_SNOW");
            str = str.replace("BLOCK_GRAVEL_BREAK", "DIG_GRAVEL");
            str = str.replace("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
            str = str.replace("ENTITY_SNOWBALL_THROW", "SHOOT_ARROW");
            str = str.replace("PLAYER_ATTACK_CRIT", "ITEM_BREAK");
            str = str.replace("ENDERMEN", "ENDERMAN");
            str = str.replace("ARROW_SHOOT", "SHOOT_ARROW");
            str = str.replace("ENDERMAN_HURT", "ENDERMAN_HIT");
            str = str.replace("BLAZE_HURT", "BLAZE_HIT");
            str = str.replace("_FLAP", "_WINGS");
            str = str.replaceAll("ENTITY_|GENERIC_|BLOCK_|_AMBIENT|_BREAK|UI_BUTTON_|EXPERIENCE_", "");
        }
        return str;
    }
}
