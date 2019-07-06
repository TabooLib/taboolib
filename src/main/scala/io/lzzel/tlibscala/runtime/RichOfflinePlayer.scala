package io.lzzel.tlibscala.runtime

import io.izzel.taboolib.module.compat.EconomyHook
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

class RichOfflinePlayer(private val offlinePlayer: OfflinePlayer) {

  def getSkullItem: ItemStack = new ItemBuilder(offlinePlayer).build()

  def getMoney: Double = EconomyHook.get(offlinePlayer)

  def withdraw(x: Double): Unit = EconomyHook.remove(offlinePlayer, x)

  def deposit(x: Double): Unit = EconomyHook.add(offlinePlayer, x)

  def hasMoney(x: Double): Boolean = EconomyHook.get(offlinePlayer) >= x

}

object RichOfflinePlayer {

  implicit def player2rich(player: OfflinePlayer): RichOfflinePlayer = new RichOfflinePlayer(player)

  implicit def rich2player(player: RichOfflinePlayer): OfflinePlayer = player.offlinePlayer

}