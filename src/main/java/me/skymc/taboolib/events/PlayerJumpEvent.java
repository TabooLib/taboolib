package me.skymc.taboolib.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJumpEvent
  extends Event
  implements Cancellable
{
  private static final HandlerList handlers = new HandlerList();
  private boolean isCancelled;
  private Player player;
  
  public PlayerJumpEvent(boolean b, Player player)
  {
    this.isCancelled = false;
    this.player = player;
  }
  
  public Player getPlayer()
  {
    return this.player;
  }
  
  public boolean isCancelled()
  {
    return this.isCancelled;
  }
  
  public void setCancelled(boolean e)
  {
    this.isCancelled = e;
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
