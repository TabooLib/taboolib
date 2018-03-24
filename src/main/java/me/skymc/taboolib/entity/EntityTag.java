package me.skymc.taboolib.entity;

import me.skymc.taboolib.Main;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 伪 - MetaData
 * 
 * @author sky
 * @since 2018-03-11 11:43:41
 */
public class EntityTag {
	
	private static EntityTag inst;
	private static ConcurrentHashMap<UUID, ConcurrentHashMap<String, Object>> entityData = new ConcurrentHashMap<>();
	
	private EntityTag() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (UUID uuid : entityData.keySet()) {
					if (EntityUtils.getEntityWithUUID(uuid) == null) {
						entityData.remove(uuid);
					}
				}
			}
		}.runTaskTimerAsynchronously(Main.getInst(), 20 * 180, 20 * 180);
	}
	
	public static EntityTag getInst() {
		if (inst == null) {
			synchronized (EntityTag.class) {
				if (inst == null) {
					inst = new EntityTag();
				}
			}
		}
		return inst;
	}
	
	/**
	 * 设置标签
	 * 
	 * @param entity 实体
	 * @param key 键
	 * @param value 值
	 */
	public void set(String key, Object value, Entity entity) {
		if (contains(entity)) {
			entityData.get(entity.getUniqueId()).put(key, value);
		} else {
			ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
			map.put(key, value);
			entityData.put(entity.getUniqueId(), map);
		}
	}
	
	/**
	 * 设置标签
	 * 
	 * @param key 键
	 * @param value 值
     */
	public void set(String key, Object value, Entity... entities) {
		for (Entity entity : entities) set(key, value, entity);
	}
	
	/**
	 * 设置标签
	 * 
	 * @param key 键
	 * @param value 值
     */
	public void set(String key, Object value, List<Entity> entities) {
		for (Entity entity : entities) set(key, value, entity);
	}
	
	/**
	 * 移除标签
	 * 
	 * @param key 键
	 * @param entity 实体
	 */
	public Object remove(String key, Entity entity) {
		if (contains(entity)) {
			entityData.get(entity.getUniqueId()).remove(key);
			if (entityData.get(entity.getUniqueId()).size() == 0) {
				return entityData.remove(entity.getUniqueId());
			}
		}
		return null;
	}
	
	/**
	 * 移除标签
	 * 
	 * @param key 键
	 * @param entities 实体
	 */
	public void remove(String key, Entity... entities) {
		for (Entity entity : entities) remove(key, entity);
	}
	
	/**
	 * 移除标签
	 * 
	 * @param key 键
	 * @param entities 实体
	 */
	public void remove(String key, List<Entity> entities) {
		for (Entity entity : entities) remove(key, entity);
	}
	
	/**
	 * 检查数据
	 * 
	 * @param entity 实体
	 * @return boolean
	 */
	public boolean contains(Entity entity) {
		return entityData.containsKey(entity.getUniqueId());
	}
	
	/**
	 * 检查标签
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return boolean
	 */
	public boolean hasKey(String key, Entity entity) {
        return contains(entity) && entityData.get(entity.getUniqueId()).containsKey(key);
    }
	
	/**
	 * 获取数据
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return Object
	 */
	public Object get(String key, Entity entity) {
		if (contains(entity)) {
			return entityData.get(entity.getUniqueId()).get(key);
		}
		return null;
	}
	
	/**
	 * 获取数据
	 * 
	 * @param key 键
	 * @param entity 值
	 * @return String
	 */
	public String getString(String key, Entity entity) {
		Object object = get(key, entity);
		if (object instanceof String) {
			return (String) object;
		}
		return null;
	}
	
	/**
	 * 获取数据
	 * 
	 * @param key 键
	 * @param entity 值
	 * @return int
	 */
	public int getInteger(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (int) object;
		}
		return 0;
	}
	
	/**
	 * 获取数据
	 * 
	 * @param key 键
	 * @param entity 值
	 * @return long
	 */
	public long getLong(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (long) object;
		}
		return 0L;
	}
	
	/**
	 * 获取数据
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return boolean
	 */
	public boolean getBoolean(String key, Entity entity) {
        Object object = get(key, entity);
        return object != null && (boolean) object;
    }
	
	/**
	 * 获取数据 
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return double
	 */
	public double getDouble(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (double) object;
		}
		return 0D;
	}
	
	/**
	 * 获取数据 
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return float
	 */
	public double getFloat(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (float) object;
		}
		return 0F;
	}
	
	/**
	 * 获取数据 
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return short
	 */
	public short getShort(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (short) object;
		}
		return (short) 0;
	}
	
	/**
	 * 获取数据 
	 * 
	 * @param key 键
	 * @param entity 实体
	 * @return byte
	 */
	public byte getByte(String key, Entity entity) {
		Object object = get(key, entity);
		if (object != null) {
			return (byte) object;
		}
		return (byte) 0;
	}
}
