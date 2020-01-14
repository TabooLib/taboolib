package io.izzel.taboolib.util.lite;

import io.izzel.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SoundPack {

    private Sound sound;
    private Float a;
    private Float b;
    private int delay;

    public SoundPack() {
        this.sound = Sounds.ENTITY_VILLAGER_NO.parseSound();
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
            this.sound = Sounds.ENTITY_VILLAGER_NO.parseSound();
            this.a = 1.0F;
            this.b = 1.0F;
            this.delay = 0;
        }
    }

    public static String getModifiedSound(String str) {
        Optional<Sound> sound = Sounds.parseSound(str);
        return sound.map(Enum::name).orElse(str);
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
