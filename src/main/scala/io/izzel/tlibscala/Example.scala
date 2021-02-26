package io.izzel.tlibscala

import io.izzel.tlibscala.Prelude._
import org.bukkit.Material
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class Example extends JavaPlugin with Listener {

  implicit lazy val plugin: Example = this

  @EventHandler
  def onJoin(event: PlayerJoinEvent): Unit = {
    event.getPlayer.sendActionBar("2333")
    event.getPlayer.displaySidebarUnranked("", "", "")
    event.getPlayer.setVelocity(1.0, 2.0, 3.0)
    event.getPlayer.withdraw(100)
    event.getPlayer.getInventory.addItem(new ItemStack(Material.DIAMOND))
    event.getPlayer << "locale.node" << "node.2"
    event.getPlayer.teleport(event.getPlayer.getLocation + (1, 2, 3))
    Task {
      event.getPlayer << "test"
    }
    AsyncTask(20) {
      event.getPlayer << "later"
    }
    class a
    assert(this == JavaPlugin.getProvidingPlugin(classOf[a]))
  }
}
