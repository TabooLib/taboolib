package com.ilummc.tlibscala.runtime

import me.skymc.taboolib.Main
import me.skymc.taboolib.economy.EcoUtils
import me.skymc.taboolib.inventory.builder.ItemBuilder
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

class RichOfflinePlayer(private val offlinePlayer: OfflinePlayer) {

  def getSkullItem: ItemStack = new ItemBuilder(offlinePlayer).build()

  def getMoney: Double = EcoUtils.get(offlinePlayer)

  def withdraw(x: Double): Boolean = Main.getEconomy.withdrawPlayer(offlinePlayer, x).transactionSuccess

  def deposit(x: Double): Boolean = Main.getEconomy.depositPlayer(offlinePlayer, x).transactionSuccess

  def hasMoney(x: Double): Boolean = Main.getEconomy.has(offlinePlayer, x)

}


object RichOfflinePlayer {

  implicit def player2rich(player: OfflinePlayer): RichOfflinePlayer = new RichOfflinePlayer(player)

  implicit def rich2player(player: RichOfflinePlayer): OfflinePlayer = player.offlinePlayer

}