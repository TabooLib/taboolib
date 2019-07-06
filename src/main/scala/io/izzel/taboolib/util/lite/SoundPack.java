package io.izzel.taboolib.util.lite;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundPack {

    private Sound sound;
    private Float a;
    private Float b;
    private int delay;

    public SoundPack() {
        this.sound = Sound.valueOf(getModifiedSound("ENTITY_VILLAGER_NO"));
        this.a = 1.0F;
        this.b = 1.0F;
    }

    public SoundPack(Sound sound, float a, float b) {
        this(sound, a, b, 0);
    }

    public SoundPack(Sound sound, float a, float b, int delay) {
        this.sound = sound;
        this.a = a;
        this.b = b;
        this.delay = delay;
    }

    public SoundPack(String s) {
        parse(s);
    }

    public void play(Player p) {
        Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), () -> p.playSound(p.getLocation(), this.sound, this.a, this.b), delay);
    }

    public void play(Location l) {
        Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), () -> l.getWorld().playSound(l, this.sound, this.a, this.b), delay);
    }

    public void parse(String s) {
        try {
            String[] split = s.split("-");
            this.sound = Sound.valueOf(getModifiedSound(split[0]));
            this.a = Float.parseFloat(split[1]);
            this.b = Float.parseFloat(split[2]);
            this.delay = split.length > 3 ? Integer.parseInt(split[3]) : 0;
        } catch (Exception var3) {
            this.sound = Sound.valueOf(getModifiedSound("ENTITY_VILLAGER_NO"));
            this.a = 1.0F;
            this.b = 1.0F;
            this.delay = 0;
        }
    }

    public static String getModifiedSound(String str) {
        if (Version.isBefore(Version.v1_9)) {
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

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public Sound getSound() {
        return sound;
    }

    public Float getA() {
        return a;
    }

    public Float getB() {
        return b;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "SoundPack{" +
                "sound=" + sound +
                ", a=" + a +
                ", b=" + b +
                ", delay=" + delay +
                '}';
    }
}
