package me.skymc.taboolib.sign;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.location.LocationUtils;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.methods.MethodsUtils;
import me.skymc.taboolib.nms.NMSUtils;

@Deprecated
public class SignUtils implements Listener {
	
	public static HashMap<String, Block> signs = new HashMap<>();
	
	public static void openSign(Player p, Block b) {
		
        if (!(b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST))) {
        	return;
        }
        
        b.setMetadata("TabooLib-Sign-Editing", new FixedMetadataValue(Main.getInst(), true));
        
        try {
	        Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
	        Object blockPos = NMSUtils.getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
	        Object sign = world.getClass().getMethod("getTileEntity", NMSUtils.getNMSClass("BlockPosition")).invoke(world, blockPos);
	
	        Object player = p.getClass().getMethod("getHandle").invoke(p);
	        
	        player.getClass().getMethod("openSign", NMSUtils.getNMSClass("TileEntitySign")).invoke(player, sign);
        }
        catch (Exception e) {
        	MsgUtils.send(p, "&4操作失败 &8(SIGN OPENED ERROR.1)");
        	e.printStackTrace();
        }
	}

    public static void openSign(Player p, String[] lines, String signid) 
    {
    	if (!MethodsUtils.checkUser(new String(new byte[] { 'm', 'e', '.', 's', 'k', 'y', 'm', 'c' }), new Exception().getStackTrace()[1].getClassName()))
		{
			throw new Error("未经允许的方法调用");
		}
    	
		Block b = LocationUtils.findBlockByLocation(p.getLocation());
        if(b == null) {
            MsgUtils.send(p, "&4所在位置无法进行该操作 &8(NOT FOUND AIR BY SIGN)");
            return;
        }
        
        b.setData((byte) 0);
        b.setType(Material.WALL_SIGN);
        
        b.setMetadata("TabooLib-Sign", new FixedMetadataValue(Main.getInst(), true));
        b.setMetadata("TabooLib-Sign-UUID", new FixedMetadataValue(Main.getInst(), signid));
        
        try {
            Object world = b.getWorld().getClass().getMethod("getHandle").invoke(b.getWorld());
            Object blockPos = NMSUtils.getNMSClass("BlockPosition").getConstructor(int.class, int.class, int.class).newInstance(b.getX(), b.getY(), b.getZ());
            Object sign = world.getClass().getMethod("getTileEntity", NMSUtils.getNMSClass("BlockPosition")).invoke(world, blockPos);

            Object[] _lines = (Object[]) sign.getClass().getField("lines").get(sign);
            
            for (int i = 0; i < lines.length ; i++) {
            	Object object = NMSUtils.getNMSClass("ChatComponentText").getConstructor(String.class).newInstance(lines[i]);
            	_lines[i] = object;
            }
            
            Object player = p.getClass().getMethod("getHandle").invoke(p);
            
            Bukkit.getScheduler().runTaskLater(Main.getInst(), () -> {
            	if (p.isOnline()) {
		            try {
						player.getClass().getMethod("openSign", NMSUtils.getNMSClass("TileEntitySign")).invoke(player, sign);
						signs.put(p.getName(), b);
		            } 
		            catch (IllegalAccessException|SecurityException|NoSuchMethodException|InvocationTargetException|IllegalArgumentException e) {
						MsgUtils.send(p, "&4操作失败 &8(SIGN OPENED ERROR.2)");
						e.printStackTrace();
					}
				}
			}, 3);
        } 
        catch (Exception e) {
        	MsgUtils.send(p, "&4操作失败 &8(SIGN OPENED ERROR.1)");
        	e.printStackTrace();
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void signChange(SignChangeEvent e) {
    	Block block = e.getBlock();
    	if (block.hasMetadata("TabooLib-Sign")) {
    		signs.remove(e.getPlayer().getName());
    		
    		String signid = String.valueOf(block.getMetadata("TabooLib-Sign-UUID").get(0).value());
    		Bukkit.getPluginManager().callEvent(new TabooSignChangeEvent(e.getPlayer(), block, e.getLines(), signid));
    		
    		block.removeMetadata("TabooLib-Sign", Main.getInst());
    		block.removeMetadata("TabooLib-Sign-UUID", Main.getInst());
    		block.setType(Material.AIR);
    		block.setData((byte) 0);
    	}
    }
    
    @EventHandler
    public void quit(PlayerQuitEvent e) {
    	if (signs.containsKey(e.getPlayer().getName())) {
    		Block block = signs.get(e.getPlayer().getName());
    		if (block.hasMetadata("TabooLib-Sign")) {
    			block.removeMetadata("TabooLib-Sign", Main.getInst());
        		block.removeMetadata("TabooLib-Sign-UUID", Main.getInst());
        		block.setType(Material.AIR);
        		block.setData((byte) 0);
    		}
    	}
    }
    
    @EventHandler
    public void block(BlockPhysicsEvent e) {
    	Block block = e.getBlock();
    	if (block.hasMetadata("TabooLib-Sign")) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void blocku(BlockBreakEvent e) {
    	Block block = e.getBlock();
    	if (block.hasMetadata("TabooLib-Sign") && !e.getPlayer().isOp()) {
    		e.setCancelled(true);
    	}
    }
}
