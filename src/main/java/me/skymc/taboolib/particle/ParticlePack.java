package me.skymc.taboolib.particle;

import me.skymc.taboolib.TabooLib;
import org.bukkit.Location;

public class ParticlePack {
	
	public EffLib particle = EffLib.VILLAGER_HAPPY;
	
	public float x = 0F;
	public float y = 0F;
	public float z = 0F;
	public int a = 0;
	
	/**
	 * VILLAGER_HAPPY-10-1-1-1
	 * 粒子-数量-X-Y-Z
	 * 
	 * @param value
	 */
	public ParticlePack(String value) {
		try {
			particle = EffLib.valueOf(value.split("-")[0]);
			a = Integer.valueOf(value.split("-")[1]);
			x = Float.valueOf(value.split("-")[2]);
			y = Float.valueOf(value.split("-")[3]);
			z = Float.valueOf(value.split("-")[4]);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * 播放粒子
	 * 
	 * @param loc
	 */
	public void play(Location loc) {
		if (TabooLib.getVerint() > 10800) {
			particle.display(x, y, z, 0f, a, loc, 50);
		}
	}
}
