package me.skymc.taboolib.particle.pack;

import com.google.common.collect.Lists;
import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-01-11 17:35
 */
public class ParticlePack {

    private List<ParticleData> particles = Lists.newArrayList();

    public ParticlePack(String str) {
        Arrays.stream(str.split(";")).forEach(this::add);
    }

    public void add(String str) {
        particles.add(new ParticleData(str));
    }

    public void play(Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> particles.forEach(p -> p.play(location)));
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public List<ParticleData> getParticles() {
        return particles;
    }
}
