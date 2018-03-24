package me.skymc.taboolib.sign;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Deprecated
public class TabooSignChangeEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private Player player;
  private Block block;
  private String[] lines;
  private String uuid;
  
  public TabooSignChangeEvent(Player player, Block block, String[] lines, String uuid)
  {
	  this.player = player;
	  this.block = block;
	  this.lines = lines;
	  this.uuid = uuid;
  }
  
  public Player getPlayer()
  {
	  return this.player;
  }
  
  public Block getBlock() {
	  return this.block;
  }
  
  public String[] getLines() {
	  return this.lines;
  }
  
  public String getUUID() {
	  return this.uuid;
  }

  public HandlerList getHandlers()
  {
	  return handlers;
  }
  
  public static HandlerList getHandlerList()
  {
	  return handlers;
  }
}
