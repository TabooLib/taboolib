package me.skymc.taboolib.particle.pack;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.particle.EffLib;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2019-01-11 17:37
 */
public class ParticleData {

    private static final Pattern ITEM_PATTERN = Pattern.compile("(\\S+)\\((\\S+):(\\S+)\\)", Pattern.CASE_INSENSITIVE);

    private ParticleType particleType = ParticleType.NORMAL;
    private EffLib particle = EffLib.BARRIER;
    private EffLib.ParticleData particleData;
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float speed = 0;
    private int amount = 0;

    /**
     * NORMAL:
     * frame-0.1-0.1-0.1-0-10
     * ITEM_CRACK:
     * iconcrack(Stone:0)-0.1-0.1-0.1-0-10
     */
    public ParticleData(String str) {
        if (!Strings.isEmpty(str)) {
            try {
                String[] split = str.split("-");
                Matcher matcher = ITEM_PATTERN.matcher(split[0]);
                if (matcher.find()) {
                    particle = EffLib.fromName(matcher.group(1));
                    particleType = ParticleType.ITEM;
                    particleData = isBlockParticle(particle.getName()) ? new EffLib.BlockData(ItemUtils.asMaterial(matcher.group(2).toUpperCase()), NumberConversions.toByte(matcher.group(3))) : new EffLib.ItemData(ItemUtils.asMaterial(matcher.group(2).toUpperCase()), NumberConversions.toByte(matcher.group(3)));
                } else {
                    particle = EffLib.fromName(split[0]);
                }
                x = NumberConversions.toFloat(split[1]);
                y = NumberConversions.toFloat(split[2]);
                z = NumberConversions.toFloat(split[3]);
                speed = NumberConversions.toFloat(split[4]);
                amount = NumberConversions.toInt(split[5]);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void play(Location location) {
        try {
            switch (particleType) {
                case NORMAL:
                    particle.display(x, y, z, speed, amount, location, 50);
                    break;
                case ITEM:
                    particle.display(particleData, x, y, z, speed, amount, location, 50);
                    break;
                default:
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean isBlockParticle(String name) {
        return name.equalsIgnoreCase("blockdust") || name.equalsIgnoreCase("blockcrack") || name.equalsIgnoreCase("fallingdust");
    }
}
