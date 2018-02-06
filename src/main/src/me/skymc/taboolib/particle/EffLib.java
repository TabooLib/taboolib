package me.skymc.taboolib.particle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.skymc.taboolib.methods.ReflectionUtils;

/**
 * 来自项目 ParticleEffect
 */
public enum EffLib {
  
    EXPLOSION_NORMAL("explode", 0, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    EXPLOSION_LARGE("largeexplode", 1, -1, new ParticleProperty[0]),  

    EXPLOSION_HUGE("hugeexplosion", 2, -1, new ParticleProperty[0]),  

    FIREWORKS_SPARK("fireworksSpark", 3, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }), 
  
    WATER_BUBBLE("bubble", 4, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_WATER }),  

    WATER_SPLASH("splash", 5, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  
    
    WATER_WAKE("wake", 6, 7, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    SUSPENDED("suspended", 7, -1, new ParticleProperty[] { ParticleProperty.REQUIRES_WATER }),  
  
    SUSPENDED_DEPTH("depthSuspend", 8, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    CRIT("crit", 9, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    CRIT_MAGIC("magicCrit", 10, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    SMOKE_NORMAL("smoke", 11, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    SMOKE_LARGE("largesmoke", 12, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    SPELL("spell", 13, -1, new ParticleProperty[0]), 

    SPELL_INSTANT("instantSpell", 14, -1, new ParticleProperty[0]),  

    SPELL_MOB("mobSpell", 15, -1, new ParticleProperty[] { ParticleProperty.COLORABLE }),  

    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, -1, new ParticleProperty[] { ParticleProperty.COLORABLE }),  

    SPELL_WITCH("witchMagic", 17, -1, new ParticleProperty[0]),  

    DRIP_WATER("dripWater", 18, -1, new ParticleProperty[0]),  

    DRIP_LAVA("dripLava", 19, -1, new ParticleProperty[0]),  

    VILLAGER_ANGRY("angryVillager", 20, -1, new ParticleProperty[0]),  

    VILLAGER_HAPPY("happyVillager", 21, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    TOWN_AURA("townaura", 22, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  

    NOTE("note", 23, -1, new ParticleProperty[] { ParticleProperty.COLORABLE }),  
    
    PORTAL("portal", 24, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  
    
    ENCHANTMENT_TABLE("enchantmenttable", 25, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  
    
    FLAME("flame", 26, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  
    
    LAVA("lava", 27, -1, new ParticleProperty[0]),  
    
    FOOTSTEP("footstep", 28, -1, new ParticleProperty[0]),  
    
    CLOUD("cloud", 29, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }), 
    
    REDSTONE("reddust", 30, -1, new ParticleProperty[] { ParticleProperty.COLORABLE }), 
    
    SNOWBALL( "snowballpoof", 31, -1, new ParticleProperty[0]),  
    
    SNOW_SHOVEL( "snowshovel", 32, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL }),  
    
    SLIME("slime", 33, -1, new ParticleProperty[0]),  
    
    HEART("heart", 34, -1, new ParticleProperty[0]),  
    
    BARRIER("barrier", 35, 8, new ParticleProperty[0]),  
    
    ITEM_CRACK("iconcrack", 36, -1, new ParticleProperty[] { ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_DATA }),  
    
    BLOCK_CRACK("blockcrack", 37, -1, new ParticleProperty[] { ParticleProperty.REQUIRES_DATA }),  
    
    BLOCK_DUST("blockdust", 38, 7, new ParticleProperty[] { ParticleProperty.DIRECTIONAL, ParticleProperty.REQUIRES_DATA }),  
    
    WATER_DROP("droplet", 39, 8, new ParticleProperty[0]),  
    
    ITEM_TAKE("take", 40, 8, new ParticleProperty[0]),  
    
    MOB_APPEARANCE("mobappearance", 41, 8, new ParticleProperty[0]),
    
    DRAGON_BREATH("dragonbreath", 42, -1, new ParticleProperty[0]),
    
    END_ROD("endrod", 43, -1, new ParticleProperty[0]),
    
    DAMAGE_INDICATOR("damageIndicator", 44, -1, new ParticleProperty[0]),
    
    SWEEP_ATTACK("sweepAttack", 45, 8, new ParticleProperty[0]);
  
	private static final Map<String, EffLib> NAME_MAP;
	private static final Map<Integer, EffLib> ID_MAP;
	private final String name;
	private final int id;
	private final int requiredVersion;
	private final List<ParticleProperty> properties;
	
	static
	{
		NAME_MAP = new HashMap<>();
		ID_MAP = new HashMap<>();
		for (EffLib localEff : values())
		{
			NAME_MAP.put(localEff.name, localEff);
			ID_MAP.put(Integer.valueOf(localEff.id), localEff);
		}
	}
  
	private EffLib(String paramString, int paramInt1, int paramInt2, ParticleProperty... paramVarArgs)
	{
		this.name = paramString;
		this.id = paramInt1;
		this.requiredVersion = paramInt2;
		this.properties = Arrays.asList(paramVarArgs);
	}
  
	public String getName()
	{
		return this.name;
	}
  
	public int getId()
  	{
		return this.id;
  	}
  
	public int getRequiredVersion()
 	{
		return this.requiredVersion;
 	}
  
	public boolean hasProperty(ParticleProperty paramParticleProperty)
	{
		return this.properties.contains(paramParticleProperty);
	}
  
	public boolean isSupported()
	{
		if (this.requiredVersion == -1) {
			return true;
		}
		return ParticlePacket.getVersion() >= this.requiredVersion;
	}
  
	public static EffLib fromName(String paramString)
	{
		for (Map.Entry localEntry : NAME_MAP.entrySet()) {
			if (((String)localEntry.getKey()).equalsIgnoreCase(paramString)) {
				return (EffLib)localEntry.getValue();
			}
		}
		return null;
	}
  
	public static EffLib fromId(int paramInt)
	{
		for (Map.Entry localEntry : ID_MAP.entrySet()) {
			if (((Integer)localEntry.getKey()).intValue() == paramInt) {
				return (EffLib)localEntry.getValue();
			}
		}
		return null;
	}
  
	private static boolean isWater(Location paramLocation)
 	{
		Material localMaterial = paramLocation.getBlock().getType();
		return (localMaterial == Material.WATER) || (localMaterial == Material.STATIONARY_WATER);
 	}
  
	private static boolean isLongDistance(Location paramLocation, List<Player> paramList)
	{
		String str = paramLocation.getWorld().getName();
		for (Player localPlayer : paramList)
		{
			Location localLocation = localPlayer.getLocation();
			if ((str.equals(localLocation.getWorld().getName())) && (localLocation.distanceSquared(paramLocation) >= 65536.0D)) {
				return true;
			}
		}
		return false;
	}
  
	private static boolean isDataCorrect(EffLib paramEff, ParticleData paramParticleData)
	{
		return ((paramEff == BLOCK_CRACK) || (paramEff == BLOCK_DUST)) && (((paramParticleData instanceof BlockData)) || ((paramEff == ITEM_CRACK) && ((paramParticleData instanceof ItemData))));
	}
  
	private static boolean isColorCorrect(EffLib paramEff, ParticleColor paramParticleColor)
	{
		return ((paramEff == SPELL_MOB) || (paramEff == SPELL_MOB_AMBIENT) || (paramEff == REDSTONE)) && (((paramParticleColor instanceof OrdinaryColor)) || ((paramEff == NOTE) && ((paramParticleColor instanceof NoteColor))));
	}
  
	public void display(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, int paramInt, Location paramLocation, double paramDouble)
	{
		if (!isSupported()) {
			throw new ParticleVersionException("This particle effect is not supported by your server version");
		}
		if (hasProperty(ParticleProperty.REQUIRES_DATA)) {
			throw new ParticleDataException("This particle effect requires additional data");
		}
		if ((hasProperty(ParticleProperty.REQUIRES_WATER)) && (!isWater(paramLocation))) {
			throw new IllegalArgumentException("There is no water at the center location");
		}
		new ParticlePacket(this, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, paramDouble > 256.0D, null).sendTo(paramLocation, paramDouble);
	}
  
    public void display(final float paramFloat1, final float paramFloat2, final float paramFloat3, final float paramFloat4, final int paramInt, final Location paramLocation, final List<Player> paramList) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !isWater(paramLocation)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, isLongDistance(paramLocation, paramList), null).sendTo(paramLocation, paramList);
    }
    
    public void display(final float paramFloat1, final float paramFloat2, final float paramFloat3, final float paramFloat4, final int paramInt, final Location paramLocation, final Player... paramVarArgs) {
        this.display(paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, paramLocation, Arrays.asList(paramVarArgs));
    }
    
    public void display(final Vector paramVector, final float paramFloat, final Location paramLocation, final double paramDouble) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (!this.hasProperty(ParticleProperty.DIRECTIONAL)) {
            throw new IllegalArgumentException("This particle effect is not directional");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !isWater(paramLocation)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, paramVector, paramFloat, paramDouble > 256.0, null).sendTo(paramLocation, paramDouble);
    }
    
    public void display(final Vector paramVector, final float paramFloat, final Location paramLocation, final List<Player> paramList) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect requires additional data");
        }
        if (!this.hasProperty(ParticleProperty.DIRECTIONAL)) {
            throw new IllegalArgumentException("This particle effect is not directional");
        }
        if (this.hasProperty(ParticleProperty.REQUIRES_WATER) && !isWater(paramLocation)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }
        new ParticlePacket(this, paramVector, paramFloat, isLongDistance(paramLocation, paramList), null).sendTo(paramLocation, paramList);
    }
    
    public void display(final Vector paramVector, final float paramFloat, final Location paramLocation, final Player... paramVarArgs) {
        this.display(paramVector, paramFloat, paramLocation, Arrays.asList(paramVarArgs));
    }
    
    public void display(final ParticleColor paramParticleColor, final Location paramLocation, final double paramDouble) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!isColorCorrect(this, paramParticleColor)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, paramParticleColor, paramDouble > 256.0).sendTo(paramLocation, paramDouble);
    }
    
    public void display(final ParticleColor paramParticleColor, final Location paramLocation, final List<Player> paramList) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.COLORABLE)) {
            throw new ParticleColorException("This particle effect is not colorable");
        }
        if (!isColorCorrect(this, paramParticleColor)) {
            throw new ParticleColorException("The particle color type is incorrect");
        }
        new ParticlePacket(this, paramParticleColor, isLongDistance(paramLocation, paramList)).sendTo(paramLocation, paramList);
    }
    
    public void display(final ParticleColor paramParticleColor, final Location paramLocation, final Player... paramVarArgs) {
        this.display(paramParticleColor, paramLocation, Arrays.asList(paramVarArgs));
    }
    
    public void display(final ParticleData paramParticleData, final float paramFloat1, final float paramFloat2, final float paramFloat3, final float paramFloat4, final int paramInt, final Location paramLocation, final double paramDouble) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!isDataCorrect(this, paramParticleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, paramDouble > 256.0, paramParticleData).sendTo(paramLocation, paramDouble);
    }
    
    public void display(final ParticleData paramParticleData, final float paramFloat1, final float paramFloat2, final float paramFloat3, final float paramFloat4, final int paramInt, final Location paramLocation, final List<Player> paramList) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!isDataCorrect(this, paramParticleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, isLongDistance(paramLocation, paramList), paramParticleData).sendTo(paramLocation, paramList);
    }
    
    public void display(final ParticleData paramParticleData, final float paramFloat1, final float paramFloat2, final float paramFloat3, final float paramFloat4, final int paramInt, final Location paramLocation, final Player... paramVarArgs) {
        this.display(paramParticleData, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramInt, paramLocation, Arrays.asList(paramVarArgs));
    }
    
    public void display(final ParticleData paramParticleData, final Vector paramVector, final float paramFloat, final Location paramLocation, final double paramDouble) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!isDataCorrect(this, paramParticleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, paramVector, paramFloat, paramDouble > 256.0, paramParticleData).sendTo(paramLocation, paramDouble);
    }
    
    public void display(final ParticleData paramParticleData, final Vector paramVector, final float paramFloat, final Location paramLocation, final List<Player> paramList) {
        if (!this.isSupported()) {
            throw new ParticleVersionException("This particle effect is not supported by your server version");
        }
        if (!this.hasProperty(ParticleProperty.REQUIRES_DATA)) {
            throw new ParticleDataException("This particle effect does not require additional data");
        }
        if (!isDataCorrect(this, paramParticleData)) {
            throw new ParticleDataException("The particle data type is incorrect");
        }
        new ParticlePacket(this, paramVector, paramFloat, isLongDistance(paramLocation, paramList), paramParticleData).sendTo(paramLocation, paramList);
    }
    
    public void display(final ParticleData paramParticleData, final Vector paramVector, final float paramFloat, final Location paramLocation, final Player... paramVarArgs) {
        this.display(paramParticleData, paramVector, paramFloat, paramLocation, Arrays.asList(paramVarArgs));
    }
  
    public static enum ParticleProperty
    {
    	REQUIRES_WATER,  REQUIRES_DATA,  DIRECTIONAL,  COLORABLE;
    }
  
    public static abstract class ParticleData
    {
    	private final Material material;
    	private final byte data;
    	private final int[] packetData;
    
    	public ParticleData(Material paramMaterial, byte paramByte)
    	{
    		this.material = paramMaterial;
    		this.data = paramByte;
    		this.packetData = new int[] { paramMaterial.getId(), paramByte };
    	}
    
    	public Material getMaterial()
    	{
    		return this.material;
    	}
    
    	public byte getData()
    	{
    		return this.data;
		}
    
    	public int[] getPacketData()
    	{
    		return this.packetData;
    	}
    
    	public String getPacketDataString()
    	{
    		return "_" + this.packetData[0] + "_" + this.packetData[1];
    	}
	}
  
	public static final class ItemData extends EffLib.ParticleData 
	{
		public ItemData(Material paramMaterial, byte paramByte)
		{
			super(paramMaterial, paramByte);
		}
	}
  
	public static final class BlockData extends EffLib.ParticleData 
	{
		public BlockData(Material paramMaterial, byte paramByte) {
			super(paramMaterial, paramByte);
			 
			if (!paramMaterial.isBlock()) {
				throw new IllegalArgumentException("The material is not a block");
			}
		}
    
		public BlockData(Material paramMaterial) 
		{
			super(paramMaterial, (byte)0);
			if (!paramMaterial.isBlock()) {
				throw new IllegalArgumentException("The material is not a block");
			}
		}
	}
  
	public static final class OrdinaryColor extends EffLib.ParticleColor
	{
		private final int red;
		private final int green;
		private final int blue;
    
		public OrdinaryColor(int paramInt1, int paramInt2, int paramInt3)
		{
			if (paramInt1 < 0) {
				throw new IllegalArgumentException("The red value is lower than 0");
			}
			if (paramInt1 > 255) {
				throw new IllegalArgumentException("The red value is higher than 255");
			}
			this.red = paramInt1;
			if (paramInt2 < 0) {
				throw new IllegalArgumentException("The green value is lower than 0");
			}
			if (paramInt2 > 255) {
				throw new IllegalArgumentException("The green value is higher than 255");
			}
			this.green = paramInt2;
			if (paramInt3 < 0) {
				throw new IllegalArgumentException("The blue value is lower than 0");
			}
			if (paramInt3 > 255) {
				throw new IllegalArgumentException("The blue value is higher than 255");
			}
			this.blue = paramInt3;
		}
    
		public OrdinaryColor(Color paramColor)
		{
			this(paramColor.getRed(), paramColor.getGreen(), paramColor.getBlue());
		}
    
		public int getRed()
		{
			return this.red;
		}
    
		public int getGreen()
		{
			return this.green;
		}
    
		public int getBlue()
		{
			return this.blue;
		}
		 
		public float getValueX()
		{
			return this.red / 255.0F;
		}
    
		public float getValueY()
		{
			return this.green / 255.0F;
		}
    
		public float getValueZ()
		{
			return this.blue / 255.0F;
		}
	}
  
	 public static final class NoteColor extends EffLib.ParticleColor
	 {
		 private final int note;
    
		 public NoteColor(int paramInt)
		 {
			 if (paramInt < 0) {
				 throw new IllegalArgumentException("The note value is lower than 0");
			 }
			 if (paramInt > 24) {
				 throw new IllegalArgumentException("The note value is higher than 24");
			 }
			 this.note = paramInt;
		 }
    
		 public float getValueX()
		 {
			 return this.note / 24.0F;
		 }
    
		 public float getValueY()
		 {
			 return 0.0F;
		 }
    
		 public float getValueZ()
		 {
			 return 0.0F;
		 }
	 }
  
	 private static final class ParticleDataException extends RuntimeException
	 {
		 private static final long serialVersionUID = 3203085387160737484L;
    
		 public ParticleDataException(String paramString)
		 {
			 super();
		 }
	 }	
  
	private static final class ParticleColorException extends RuntimeException
	{
		private static final long serialVersionUID = 3203085387160737484L;
    
		public ParticleColorException(String paramString)
		{
			super();
		}
	}
  
	private static final class ParticleVersionException extends RuntimeException
	{
		private static final long serialVersionUID = 3203085387160737484L;
    
		public ParticleVersionException(String paramString)
		{
			super();
		}
	}
  
	public static final class ParticlePacket
	{
		private static int version;
		private static Class<?> enumParticle;
		private static Constructor<?> packetConstructor;
		private static Method getHandle;
		private static Field playerConnection;
		private static Method sendPacket;
		private static boolean initialized;
		private final EffLib effect;
		private float offsetX;
		private final float offsetY;
		private final float offsetZ;
		private final float speed;
		private final int amount;
		private final boolean longDistance;
		private final EffLib.ParticleData data;
		private Object packet;
    
		public ParticlePacket(EffLib paramEff, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, int paramInt, boolean paramBoolean, EffLib.ParticleData paramParticleData)
		{
			initialize();
			if (paramFloat4 < 0.0F) {
				throw new IllegalArgumentException("The speed is lower than 0");
			}
			if (paramInt < 0) {
				throw new IllegalArgumentException("The amount is lower than 0");
			}
			this.effect = paramEff;
			this.offsetX = paramFloat1;
			this.offsetY = paramFloat2;
			this.offsetZ = paramFloat3;
			this.speed = paramFloat4;
			this.amount = paramInt;
			this.longDistance = paramBoolean;
			this.data = paramParticleData;
		}
    
		public ParticlePacket(EffLib paramEff, Vector paramVector, float paramFloat, boolean paramBoolean, EffLib.ParticleData paramParticleData)
		{
			this(paramEff, (float)paramVector.getX(), (float)paramVector.getY(), (float)paramVector.getZ(), paramFloat, 0, paramBoolean, paramParticleData);
		}
    
		public ParticlePacket(EffLib paramEff, EffLib.ParticleColor paramParticleColor, boolean paramBoolean)
		{
			this(paramEff, paramParticleColor.getValueX(), paramParticleColor.getValueY(), paramParticleColor.getValueZ(), 1.0F, 0, paramBoolean, null);
			if ((paramEff == EffLib.REDSTONE) && ((paramParticleColor instanceof EffLib.OrdinaryColor)) && (((EffLib.OrdinaryColor)paramParticleColor).getRed() == 0)) {
				this.offsetX = 1.175494E-038F;
			}
		}
    
		public static void initialize()
		{
			if (initialized) {
				return;
			}
			try
			{
				version = Integer.parseInt(ReflectionUtils.PackageType.getServerVersion().split("_")[1]);
				if (version > 7) {
					enumParticle = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
				}
				Class<? extends Object> localClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
				packetConstructor = ReflectionUtils.getConstructor(localClass, new Class[0]);
				getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle", new Class[0]);
				playerConnection = ReflectionUtils.getField("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, false, "playerConnection");
				sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", new Class[] { ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Packet") });
			}
			catch (Exception localException)
			{
				throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", localException);
			}
			initialized = true;
		}
    
		public static int getVersion()
		{
			if (!initialized) {
				initialize();
			}
			return version;
		}
    
        private void initializePacket(final Location paramLocation) {
            if (this.packet != null) {
                return;
            }
            try {
                this.packet = ParticlePacket.packetConstructor.newInstance(new Object[0]);
                if (ParticlePacket.version < 8) {
                    Object localObject = this.effect.getName();
                    if (this.data != null) {
                        localObject = String.valueOf(localObject) + this.data.getPacketDataString();
                    }
                    ReflectionUtils.setValue(this.packet, true, "a", localObject);
                }
                else {
                    ReflectionUtils.setValue(this.packet, true, "a", ParticlePacket.enumParticle.getEnumConstants()[this.effect.getId()]);
                    ReflectionUtils.setValue(this.packet, true, "j", this.longDistance);
                    if (this.data != null) {
                        final Object localObject = this.data.getPacketData()[0];
                        ReflectionUtils.setValue(this.packet, true, "k", new int[] { (int) localObject });
                    }
                }
                ReflectionUtils.setValue(this.packet, true, "b", (float)paramLocation.getX());
                ReflectionUtils.setValue(this.packet, true, "c", (float)paramLocation.getY());
                ReflectionUtils.setValue(this.packet, true, "d", (float)paramLocation.getZ());
                ReflectionUtils.setValue(this.packet, true, "e", this.offsetX);
                ReflectionUtils.setValue(this.packet, true, "f", this.offsetY);
                ReflectionUtils.setValue(this.packet, true, "g", this.offsetZ);
                ReflectionUtils.setValue(this.packet, true, "h", this.speed);
                ReflectionUtils.setValue(this.packet, true, "i", this.amount);
            }
            catch (Exception localException) {
                throw new PacketInstantiationException("Packet instantiation failed", localException);
            }
        }
        
        public void sendTo(final Location paramLocation, final Player paramPlayer) {
            this.initializePacket(paramLocation);
            try {
                ParticlePacket.sendPacket.invoke(ParticlePacket.playerConnection.get(ParticlePacket.getHandle.invoke(paramPlayer, new Object[0])), this.packet);
            }
            catch (Exception localException) {
                throw new PacketSendingException("Failed to send the packet to player '" + paramPlayer.getName() + "'", localException);
            }
        }
        
        public void sendTo(final Location paramLocation, final List<Player> paramList) {
            if (paramList.isEmpty()) {
                throw new IllegalArgumentException("The player list is empty");
            }
            for (final Player localPlayer : paramList) {
                this.sendTo(paramLocation, localPlayer);
            }
        }
        
        public void sendTo(final Location paramLocation, final double paramDouble) {
            if (paramDouble < 1.0) {
                throw new IllegalArgumentException("The range is lower than 1");
            }
            final String str = paramLocation.getWorld().getName();
            final double d = paramDouble * paramDouble;
            for (final Player localPlayer : Bukkit.getOnlinePlayers()) {
                if (localPlayer.getWorld().getName().equals(str) && localPlayer.getLocation().distanceSquared(paramLocation) <= d) {
                    this.sendTo(paramLocation, localPlayer);
                }
            }
        }
        
        private static final class VersionIncompatibleException extends RuntimeException
        {
            private static final long serialVersionUID = 3203085387160737484L;
            
            public VersionIncompatibleException(final String paramString, final Throwable paramThrowable) {
                super(paramThrowable);
            }
        }
        
        private static final class PacketInstantiationException extends RuntimeException
        {
            private static final long serialVersionUID = 3203085387160737484L;
            
            public PacketInstantiationException(final String paramString, final Throwable paramThrowable) {
                super(paramThrowable);
            }
        }
        
        private static final class PacketSendingException extends RuntimeException
        {
            private static final long serialVersionUID = 3203085387160737484L;
            
            public PacketSendingException(final String paramString, final Throwable paramThrowable) {
                super(paramThrowable);
            }
        }
    }
    
    public abstract static class ParticleColor
    {
        public abstract float getValueX();
        
        public abstract float getValueY();
        
        public abstract float getValueZ();
    }
}
