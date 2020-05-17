package io.izzel.taboolib.util.lite;

import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.packet.TPacketHandler;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <b>ParticleEffect Library</b>
 * <p>
 * This library was created by @DarkBlade12 and allows you to display all Minecraft particle effects on a Bukkit server
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following conditions:
 * <ul>
 * <li>Don't claim this class as your own
 * <li>Don't remove this disclaimer
 * </ul>
 * <p>
 * Special thanks:
 * <ul>
 * <li>@microgeek (original idea, names and packet parameters)
 * <li>@ShadyPotato (1.8 names, ids and packet parameters)
 * <li>@RingOfStorms (particle behavior)
 * <li>@Cybermaxke (particle behavior)
 * <li>@JamieSinn (hosting a jenkins server and documentation for particleeffect)
 * <li>@SkytAsul (updating to 1.13)
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a published project</i>
 *
 * @author DarkBlade12
 * @version 1.8
 */
@Deprecated
public enum Particles {

    BARRIER,
    BLOCK_CRACK(ParticleProperty.REQUIRES_DATA),
    BLOCK_DUST(ParticleProperty.REQUIRES_DATA),
    BUBBLE_COLUMN_UP(13),
    BUBBLE_POP(13),
    CLOUD,
    CRIT,
    CRIT_MAGIC,
    CURRENT_DOWN(13),
    DAMAGE_INDICATOR(9),
    DOLPHIN(13),
    DRAGON_BREATH(9),
    DRIP_LAVA,
    DRIP_WATER,
    ENCHANTMENT_TABLE,
    END_ROD(9),
    EXPLOSION_HUGE,
    EXPLOSION_LARGE,
    EXPLOSION_NORMAL,
    FALLING_DUST(10),
    FIREWORKS_SPARK,
    FLAME,
    FOOTSTEP(0, 12),
    HEART,
    ITEM_CRACK(ParticleProperty.REQUIRES_DATA),
    ITEM_TAKE,
    LAVA,
    MOB_APPEARANCE,
    NAUTILUS(13),
    NOTE(ParticleProperty.COLORABLE),
    PORTAL,
    REDSTONE(ParticleProperty.COLORABLE),
    SLIME,
    SMOKE_LARGE,
    SMOKE_NORMAL,
    SNOW_SHOVEL,
    SNOWBALL,
    SPELL,
    SPELL_INSTANT,
    SPELL_MOB(ParticleProperty.COLORABLE),
    SPELL_MOB_AMBIENT(ParticleProperty.COLORABLE),
    SPELL_WITCH,
    SPIT(11),
    SQUID_INK(13),
    SUSPENDED,
    SUSPENDED_DEPTH(0, 12),
    SWEEP_ATTACK(9),
    TOTEM(11),
    TOWN_AURA,
    VILLAGER_ANGRY,
    VILLAGER_HAPPY,
    WATER_BUBBLE,
    WATER_DROP,
    WATER_SPLASH,
    WATER_WAKE,
    ;

    private static final int mcVersion = NumberConversions.toInt(Version.getBukkitVersion().split("_")[1]);
    private org.bukkit.Particle bukkitParticle;
    private final List<ParticleProperty> properties;
    private final int min;
    private final int max;

    Particles(ParticleProperty... properties) {
        this(0, 0, properties);
    }

    Particles(int min, ParticleProperty... properties) {
        this(min, 0, properties);
    }

    Particles(int min, int max, ParticleProperty... properties) {
        this.properties = Arrays.asList(properties);
        this.min = min;
        this.max = max;
        try {
            bukkitParticle = org.bukkit.Particle.valueOf(this.name());
        } catch (IllegalArgumentException ex) {
            bukkitParticle = null;
        }
    }

    public org.bukkit.Particle getBukkitParticle() {
        return bukkitParticle;
    }

    public int getMinimumVersion() {
        return min;
    }

    public int getMaximumVersion() {
        return max;
    }


    /**
     * Determine if this particle effect has a specific property
     *
     * @param property Property tested
     * @return Whether it has the property or not
     */
    public boolean hasProperty(ParticleProperty property) {
        return properties.contains(property);
    }

    /**
     * Determine if this particle effect is supported by your current server version
     *
     * @return Whether the particle effect is supported or not
     */
    public boolean isSupported() {
        if (min != 0 && min > mcVersion) {
            return false;
        }
        if (max != 0 && max < mcVersion) {
            return false;
        }
        return bukkitParticle != null;
    }

    /**
     * Returns the particle effect with the given name
     *
     * @param name Name of the particle effect
     * @return The particle effect
     */
    public static Particles fromName(String name) {
        for (Particles effect : values()) {
            if (effect.name().equalsIgnoreCase(name)) {
                if (!effect.isSupported()) {
                    throw new ParticleVersionException();
                }
                return effect;
            }
        }
        throw new IllegalArgumentException("ParticleEffect " + name + " doesn't exist.");
    }

    /**
     * Determine if the distance between @param location and one of the players exceeds 256
     *
     * @param location Location to check
     * @return Whether the distance exceeds 256 or not
     */
    private static boolean isLongDistance(Location location, List<Player> players) {
        String world = location.getWorld().getName();
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            Location playerLocation = player.getLocation();
            if (!world.equals(playerLocation.getWorld().getName()) || playerLocation.distanceSquared(location) < 65536) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Determine if the data type for a particle effect is correct
     *
     * @param effect Particle effect
     * @param data   Particle data
     * @return Whether the data type is correct or not
     */
    private static boolean isDataCorrect(Particles effect, Object data) {
        return ((effect == BLOCK_CRACK || effect == BLOCK_DUST) && (mcVersion < 13 ? data instanceof MaterialData : data instanceof BlockData)) || (effect == ITEM_CRACK && data instanceof ItemStack);
    }

    /**
     * Determine if the color type for a particle effect is correct
     *
     * @param effect Particle effect
     * @param color  Particle color
     * @return Whether the color type is correct or not
     */
    private static boolean isColorCorrect(Particles effect, ParticleColor color) {
        return ((effect == SPELL_MOB || effect == SPELL_MOB_AMBIENT || effect == REDSTONE) && color instanceof OrdinaryColor) || (effect == NOTE && color instanceof NoteColor);
    }

    /**
     * Displays a particle effect which is only visible for all players within a certain range in the world of @param center
     *
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param speed   Display speed of the particles
     * @param amount  Amount of particles
     * @param center  Center location of the effect
     * @param range   Range of the visibility
     * @throws ParticleVersionException If the particle effect is not supported by the server version
     * @throws ObjectException          If the particle effect requires additional data
     * @throws IllegalArgumentException If the particle effect requires water and none is at the center location
     */
    public void display(double offsetX, double offsetY, double offsetZ, double speed, int amount, Location center, double range) throws ParticleVersionException, ObjectException, IllegalArgumentException {
        if (!isSupported()) {
            throw new ParticleVersionException();
        }
        if (hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ObjectException("This particle effect requires additional data");
        }
		/*if (hasProperty(ParticleProperty.REQUIRES_WATER) && !isWater(center)) {
			throw new IllegalArgumentException("There is no water at the center location");
		}*/
        new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, range > 256, null).sendTo(center, range);
    }

    /**
     * Displays a particle effect which is only visible for the specified players
     *
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param speed   Display speed of the particles
     * @param amount  Amount of particles
     * @param center  Center location of the effect
     * @param players Receivers of the effect
     * @throws ParticleVersionException If the particle effect is not supported by the server version
     * @throws ObjectException          If the particle effect requires additional data
     * @throws IllegalArgumentException If the particle effect requires water and none is at the center location
     */
    public void display(double offsetX, double offsetY, double offsetZ, double speed, int amount, Location center, List<Player> players) throws ParticleVersionException, ObjectException, IllegalArgumentException {
        if (!isSupported()) {
            throw new ParticleVersionException();
        }
        if (hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ObjectException("This particle effect requires additional data");
        }
		/*if (hasProperty(ParticleProperty.REQUIRES_WATER) && !isWater(center)) {
			throw new IllegalArgumentException("There is no water at the center location");
		}*/
        new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), null).sendTo(center, players);
    }

    /**
     * Displays a single particle which is colored and only visible for all players within a certain range in the world of @param center
     *
     * @param color   Color of the particle
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param amount  Amount of particles
     * @param center  Center location of the effect
     * @param range   Range of the visibility
     * @throws ParticleVersionException If the particle effect is not supported by the server version
     * @throws ParticleColorException   If the particle effect is not colorable or the color type is incorrect
     */
    public void display(ParticleColor color, double offsetX, double offsetY, double offsetZ, int amount, Location center, double range) throws ParticleVersionException, ParticleColorException {
        if (!isSupported()) {
            throw new ParticleVersionException();
        }
        if (!hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!isColorCorrect(this, color)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, offsetX, offsetY, offsetZ, 1, amount, range > 256, color).sendTo(center, range);
    }

    /**
     * Displays a single particle which is colored and only visible for the specified players
     *
     * @param color   Color of the particle
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param amount  Amount of particles
     * @param center  Center location of the effect
     * @param players Receivers of the effect
     * @throws ParticleVersionException If the particle effect is not supported by the server version
     * @throws ParticleColorException   If the particle effect is not colorable or the color type is incorrect
     */
    public void display(ParticleColor color, double offsetX, double offsetY, double offsetZ, int amount, Location center, List<Player> players) throws ParticleVersionException, ParticleColorException {
        if (!isSupported()) {
            throw new ParticleVersionException();
        }
        if (!hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!isColorCorrect(this, color)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, offsetX, offsetY, offsetZ, 1, amount, isLongDistance(center, players), color).sendTo(center, players);
    }

    /**
     * Displays a particle effect which requires additional data and is only visible for the specified players
     *
     * @param data    Data of the effect
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param speed   Display speed of the particles
     * @param amount  Amount of particles
     * @param center  Center location of the effect
     * @param players Receivers of the effect
     * @throws ParticleVersionException If the particle effect is not supported by the server version
     * @throws ObjectException          If the particle effect does not require additional data or if the data type is incorrect
     */
    public void display(Object data, double offsetX, double offsetY, double offsetZ, double speed, int amount, Location center, List<Player> players) throws ParticleVersionException, ObjectException {
        if (!isSupported()) {
            throw new ParticleVersionException();
        }
        if (!hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ObjectException("This particle effect does not require additional data");
        }
        if (!isDataCorrect(this, data)) {
            throw new ObjectException("The particle data type is incorrect");
        }
        new ParticlePacket(this, offsetX, offsetY, offsetZ, speed, amount, isLongDistance(center, players), data).sendTo(center, players);
    }

    /**
     * Represents the property of a particle effect
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.7
     */
    public enum ParticleProperty {
        /**
         * The particle effect requires water to be displayed
         */
        @Deprecated
        REQUIRES_WATER,
        /**
         * The particle effect requires block or item data to be displayed
         */
        REQUIRES_DATA,
        /**
         * The particle effect uses the offsets as direction values
         */
        @Deprecated
        DIRECTIONAL,
        /**
         * The particle effect uses the offsets as color values
         */
        COLORABLE
    }

    /**
     * Represents the color for effects like {@link Particles#SPELL_MOB}, {@link Particles#SPELL_MOB_AMBIENT}, {@link Particles#REDSTONE} and {@link Particles#NOTE}
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.7
     */
    public static abstract class ParticleColor {
        /**
         * Returns the value for the offsetX field
         *
         * @return The offsetX value
         */
        public abstract float getValueX();

        /**
         * Returns the value for the offsetY field
         *
         * @return The offsetY value
         */
        public abstract float getValueY();

        /**
         * Returns the value for the offsetZ field
         *
         * @return The offsetZ value
         */
        public abstract float getValueZ();
    }

    /**
     * Represents the color for effects like {@link Particles#SPELL_MOB}, {@link Particles#SPELL_MOB_AMBIENT} and {@link Particles#NOTE}
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.7
     */
    public static class OrdinaryColor extends ParticleColor {
        private final int red;
        private final int green;
        private final int blue;

        /**
         * Construct a new ordinary color
         *
         * @param red   Red value of the RGB format
         * @param green Green value of the RGB format
         * @param blue  Blue value of the RGB format
         * @throws IllegalArgumentException If one of the values is lower than 0 or higher than 255
         */
        public OrdinaryColor(int red, int green, int blue) throws IllegalArgumentException {
            if (red < 0) {
                throw new IllegalArgumentException("The red value is lower than 0");
            }
            if (red > 255) {
                throw new IllegalArgumentException("The red value is higher than 255");
            }
            this.red = red;
            if (green < 0) {
                throw new IllegalArgumentException("The green value is lower than 0");
            }
            if (green > 255) {
                throw new IllegalArgumentException("The green value is higher than 255");
            }
            this.green = green;
            if (blue < 0) {
                throw new IllegalArgumentException("The blue value is lower than 0");
            }
            if (blue > 255) {
                throw new IllegalArgumentException("The blue value is higher than 255");
            }
            this.blue = blue;
        }

        /**
         * Construct a new ordinary color
         *
         * @param color Bukkit color
         */
        public OrdinaryColor(Color color) {
            this(color.getRed(), color.getGreen(), color.getBlue());
        }

        /**
         * Returns the red value of the RGB format
         *
         * @return The red value
         */
        public int getRed() {
            return red;
        }

        /**
         * Returns the green value of the RGB format
         *
         * @return The green value
         */
        public int getGreen() {
            return green;
        }

        /**
         * Returns the blue value of the RGB format
         *
         * @return The blue value
         */
        public int getBlue() {
            return blue;
        }

        /**
         * Returns the red value divided by 255
         *
         * @return The offsetX value
         */

        public float getValueX() {
            return (float) red / 255F;
        }

        /**
         * Returns the green value divided by 255
         *
         * @return The offsetY value
         */

        public float getValueY() {
            return (float) green / 255F;
        }

        /**
         * Returns the blue value divided by 255
         *
         * @return The offsetZ value
         */

        public float getValueZ() {
            return (float) blue / 255F;
        }
    }

    /**
     * Represents the color for the {@link Particles#NOTE} effect
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.7
     */
    public static final class NoteColor extends ParticleColor {
        private final int note;

        /**
         * Construct a new note color
         *
         * @param note Note id which determines color
         * @throws IllegalArgumentException If the note value is lower than 0 or higher than 24
         */
        public NoteColor(int note) throws IllegalArgumentException {
            if (note < 0) {
                throw new IllegalArgumentException("The note value is lower than 0");
            }
            if (note > 24) {
                throw new IllegalArgumentException("The note value is higher than 24");
            }
            this.note = note;
        }

        /**
         * Returns the note value divided by 24
         *
         * @return The offsetX value
         */

        public float getValueX() {
            return (float) note / 24F;
        }

        /**
         * Returns zero because the offsetY value is unused
         *
         * @return zero
         */

        public float getValueY() {
            return 0;
        }

        /**
         * Returns zero because the offsetZ value is unused
         *
         * @return zero
         */

        public float getValueZ() {
            return 0;
        }

    }

    public enum ParticleShape {
        POINT, NEAR, BAR, EXCLAMATION, SPOT
    }

    public static class Particle {
        private static final Random random = new Random();

        private final Particles effect;
        private final ParticleShape shape;
        private final OrdinaryColor color;

        private final byte typeCode;

        public Particle(Particles effect, ParticleShape shape, OrdinaryColor color) {
            this.effect = effect;
            this.shape = shape;
            this.color = color;

            this.typeCode = (byte) (effect == Particles.NOTE ? 2 : (effect.hasProperty(ParticleProperty.COLORABLE) ? 1 : 0));
        }

        public String toString() {
            return effect.name() + " in shape " + shape.name() + (typeCode != 0 ? " with color \"R" + (typeCode == 1 ? color.getRed() + " G" + color.getGreen() + " B" + color.getBlue() : "random") + "\"" : "");
        }

        public void send(LivingEntity en, List<Player> p) {
            if (p.isEmpty()) {
                return;
            }

            Location lc = en.getEyeLocation();
            switch (shape) {
                case POINT:
                    sendParticle(lc.add(0, 1, 0), p, 0.1, 0.1, 0.1, 1);
                    break;
                case NEAR:
                    sendParticle(lc.add(random.nextDouble() * 1.2 - 0.6, random.nextDouble() * 2 - en.getEyeHeight(), random.nextDouble() * 1.2 - 0.6), p, 0.1, 0.1, 0.1, 1);
                    break;
                case BAR:
                    sendParticle(lc.add(0, 1, 0), p, 0.01, 0.15, 0.01, 3);
                    break;
                case EXCLAMATION:
                    sendParticle(lc.add(0, 0.9, 0), p, 0.001, 0.001, 0.001, 2); //		POINT
                    sendParticle(lc.add(0, 0.7, 0), p, 0.01, 0.2, 0.01, 4); //				BAR
                    break;
                case SPOT:
                    sendParticle(lc.add(0, 0.2, 0), p, 0.2, 0.4, 0.2, 15);
                    break;
            }
        }

        private void sendParticle(Location lc, List<Player> p, double offX, double offY, double offZ, int amount) {
            switch (typeCode) {
                case 1:
                    effect.display(color, offX, offY, offZ, amount, lc, p);
                    break;
                case 2:
                    Particles.NOTE.display(new NoteColor(random.nextInt(24)), offX, offY, offZ, amount, lc, /*p.getPlayer(),*/ p);
                    break;
                case 0:
                    effect.display(offX, offY, offZ, 0.001, amount, lc, p);
                    break;
            }
        }

        public static Particle deserialize(Map<String, Object> map) {
            return new Particle(Particles.fromName((String) map.get("particleEffect")), ParticleShape.valueOf(((String) map.get("particleShape")).toUpperCase()), new OrdinaryColor(Color.deserialize(((MemorySection) map.get("particleColor")).getValues(false))));
        }
    }

    /**
     * Represents a runtime exception that is thrown either if the displayed particle effect requires data and has none or vice-versa or if the data type is incorrect
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.6
     */
    private static final class ObjectException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        /**
         * Construct a new particle data exception
         *
         * @param message Message that will be logged
         */
        public ObjectException(String message) {
            super(message);
        }
    }

    /**
     * Represents a runtime exception that is thrown either if the displayed particle effect is not colorable or if the particle color type is incorrect
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.7
     */
    private static final class ParticleColorException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        /**
         * Construct a new particle color exception
         *
         * @param message Message that will be logged
         */
        public ParticleColorException(String message) {
            super(message);
        }
    }

    /**
     * Represents a runtime exception that is thrown if the displayed particle effect requires a newer version
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.6
     */
    private static final class ParticleVersionException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        /**
         * Construct a new particle version exception
         */
        public ParticleVersionException() {
            super("This particle effect is not supported by your server version");
        }
    }

    /**
     * Represents a particle effect packet with all attributes which is used for sending packets to the players
     * <p>
     * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
     *
     * @author DarkBlade12
     * @since 1.5
     */
    public static final class ParticlePacket {
        private static boolean initialized;
        /*private static Class<?> enumParticle;
        private static String ver;
        private static String pack;*/
        private final Particles effect;
        private float offsetX;
        private float offsetY;
        private float offsetZ;
        private final float speed;
        private int amount;
        private final int size = 1;
        private final boolean longDistance;
        private Object data;
        private Object packet;

        private int timesSending = 1;

        /**
         * Construct a new particle packet
         *
         * @param effect       Particle effect
         * @param offsetX      Maximum distance particles can fly away from the center on the x-axis
         * @param offsetY      Maximum distance particles can fly away from the center on the y-axis
         * @param offsetZ      Maximum distance particles can fly away from the center on the z-axis
         * @param speed        Display speed of the particles
         * @param amount       Amount of particles
         * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
         * @param data         Data of the effect
         * @throws IllegalArgumentException If the speed or amount is lower than 0
         */
        public ParticlePacket(Particles effect, double offsetX, double offsetY, double offsetZ, double speed, int amount, boolean longDistance, Object data) throws IllegalArgumentException {
            initialize();
            if (speed < 0) {
                throw new IllegalArgumentException("The speed is lower than 0");
            }
            if (amount < 0) {
                throw new IllegalArgumentException("The amount is lower than 0");
            }
            this.effect = effect;
            this.offsetX = (float) offsetX;
            this.offsetY = (float) offsetY;
            this.offsetZ = (float) offsetZ;
            this.speed = (float) speed;
            this.amount = amount;
            this.longDistance = longDistance;
            this.data = data;
        }

        /**
         * Construct a new particle packet of a single particle flying into a determined direction
         *
         * @param effect Particle effect
         * @param direction Direction of the particle
         * @param speed Display speed of the particle
         * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
         * @param data Data of the effect
         * @throws IllegalArgumentException If the speed is lower than 0
         */
		/*public ParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance, Object data) throws IllegalArgumentException {
			this(effect, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0, longDistance, data);
		}*/

        /**
         * Initializes and sets initialized to <code>true</code> if it succeeds
         * <p>
         * <b>Note:</b> These fields only have to be initialized once, so it will return if initialized is already set to <code>true</code>
         */
        public static void initialize() {
            if (initialized) {
                return;
            }
            initialized = true;
        }

        /**
         * Determine if packet is initialized
         *
         * @return Whether these fields are initialized or not
         */
        public static boolean isInitialized() {
            return initialized;
        }

        /**
         * Initializes packet with all set values
         *
         * @param center Center location of the effect
         * @throws PacketInstantiationException If instantion fails due to an unknown error
         */
        private void initializePacket(Location center) throws PacketInstantiationException {
            if (packet != null) {
                return;
            }
            try {
                //int tmpAmount = amount;
                if (effect.hasProperty(ParticleProperty.COLORABLE) && data instanceof ParticleColor) {
                    if (mcVersion < 13 || data instanceof NoteColor) {
                        ParticleColor color = (ParticleColor) data;
                        offsetX = color.getValueX();
                        offsetY = color.getValueY();
                        offsetZ = color.getValueZ();
                        timesSending = amount < 2 ? 1 : amount;
                        amount = 0;
                        data = null;
                        if (color instanceof OrdinaryColor && ((OrdinaryColor) color).getRed() == 0) {
                            offsetX = Float.MIN_NORMAL;
                        }
                    } else if (mcVersion >= 13 && data instanceof OrdinaryColor) {
                        data = getDustColor((OrdinaryColor) data, size);
                    }
                }
                this.packet = NMS.handle().toPacketPlayOutWorldParticles(effect.getBukkitParticle(), longDistance, (float) center.getX(), (float) center.getY(), (float) center.getZ(), offsetX, offsetY, offsetZ, speed, amount, data);
            } catch (Throwable exception) {
                throw new PacketInstantiationException("Packet instantiation failed", exception);
            }
        }

        public static Object getDustColor(OrdinaryColor color, int size) {
            try {
                return Reflection.instantiateObject(Class.forName("org.bukkit.Particle$DustOptions"), Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed()), size);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Sends the packet to a single player and caches it
         *
         * @param center Center location of the effect
         * @param player Receiver of the packet
         * @throws PacketInstantiationException If instantion fails due to an unknown error
         * @throws PacketSendingException       If sending fails due to an unknown error
         */
        public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException {
            initializePacket(center);
            try {
                if (timesSending == 1) {
                    TPacketHandler.sendPacket(player, packet);
                } else {
                    for (int i = 0; i < timesSending; i++) {
                        TPacketHandler.sendPacket(player, packet);
                    }
                }
                //sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
                //((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            } catch (Throwable exception) {
                throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
            }
        }

        /**
         * Sends the packet to all players in the list
         *
         * @param center  Center location of the effect
         * @param players Receivers of the packet
         * @throws IllegalArgumentException If the player list is empty
         */
        public void sendTo(Location center, List<Player> players) throws IllegalArgumentException {
            if (players.isEmpty()) {
                throw new IllegalArgumentException("The player list is empty");
            }
            for (Player player : players) {
                sendTo(center, player);
            }
        }

        /**
         * Sends the packet to all players in a certain range
         *
         * @param center Center location of the effect
         * @param range  Range in which players will receive the packet (Maximum range for particles is usually 16, but it can differ for some types)
         * @throws IllegalArgumentException If the range is lower than 1
         */
        public void sendTo(Location center, double range) throws IllegalArgumentException {
            if (range < 1) {
                throw new IllegalArgumentException("The range is lower than 1");
            }
            String worldName = center.getWorld().getName();
            double squared = range * range;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > squared) {
                    continue;
                }
                sendTo(center, player);
            }
        }

        /**
         * Represents a runtime exception that is thrown if packet instantiation fails
         * <p>
         * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
         *
         * @author DarkBlade12
         * @since 1.4
         */
        private static final class PacketInstantiationException extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            /**
             * Construct a new packet instantiation exception
             *
             * @param message Message that will be logged
             * @param cause   Cause of the exception
             */
            public PacketInstantiationException(String message, Throwable cause) {
                super(message, cause);
            }
        }

        /**
         * Represents a runtime exception that is thrown if packet sending fails
         * <p>
         * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
         *
         * @author DarkBlade12
         * @since 1.4
         */
        private static final class PacketSendingException extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            /**
             * Construct a new packet sending exception
             *
             * @param message Message that will be logged
             * @param cause   Cause of the exception
             */
            public PacketSendingException(String message, Throwable cause) {
                super(message, cause);
            }
        }
    }
}