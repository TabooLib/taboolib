package com.ilummc.tlibscala.runtime

import com.ilummc.tlib.resources.TLocale
import me.skymc.taboolib.anvil.AnvilContainerAPI
import me.skymc.taboolib.display.{ActionUtils, TitleUtils}
import me.skymc.taboolib.permission.PermissionUtils
import me.skymc.taboolib.player.PlayerUtils
import me.skymc.taboolib.scoreboard.ScoreboardUtil
import me.skymc.taboolib.sign.SignUtils
import org.bukkit.block.Block
import org.bukkit.entity.Player

import scala.collection.JavaConverters._

class RichPlayer(private val player: Player) extends RichOfflinePlayer(player) {

  def sendActionBar(x: String): Unit = ActionUtils.send(player, x)

  def getFishTicks: Int = PlayerUtils.getFishingTicks(PlayerUtils.getPlayerHookedFish(player))

  def setFishTicks(x: Int): Unit = PlayerUtils.setFishingTicks(PlayerUtils.getPlayerHookedFish(player), x)

  def resetData(): Unit = PlayerUtils.resetData(player, false)

  def resetData(scoreboard: Boolean): Unit = PlayerUtils.resetData(player, scoreboard)

  def displaySidebar(title: String, elements: Map[String, Integer]): Unit = ScoreboardUtil.rankedSidebarDisplay(player, title, mapAsJavaMap(elements))

  def displaySidebarUnranked(title: String, elements: Array[String]): Unit = ScoreboardUtil.unrankedSidebarDisplay(player, elements: _*)

  def displaySidebarUnranked(title: String, elements: List[String]): Unit = ScoreboardUtil.unrankedSidebarDisplay(player, elements: _*)

  def displaySidebarUnranked(title: String, elements: String*): Unit = ScoreboardUtil.unrankedSidebarDisplay(player, elements: _*)

  def openSign(block: Block): Unit = SignUtils.openSign(player, block)

  def openSign(lines: Array[String], id: String): Unit = SignUtils.openSign(player, lines, id)

  //todo TagDataHandler

  def addPermission(perm: String): Unit = PermissionUtils.addPermission(player, perm)

  def removePermission(perm: String): Unit = PermissionUtils.removePermission(player, perm)

  def sendTitle(title: String, subtitle: String, fadein: Int, stay: Int, fadeout: Int): Unit = TitleUtils.sendTitle(player, title, subtitle, fadein, stay, fadeout)

  def sendTitle(p: Player, title: String, fadeint: Int, stayt: Int, fadeoutt: Int, subtitle: String, fadeinst: Int, stayst: Int, fadeoutst: Int): Unit = TitleUtils.sendTitle(p, title, fadeint, stayt, fadeoutt, subtitle, fadeinst, stayst, fadeoutst)

  def openAnvil(): Unit = AnvilContainerAPI.openAnvil(player)

  def sendLocalizedMessage(node: String, params: String*): Unit = TLocale.sendTo(player, node, params: _*)

  def locale(node: String, params: String*): Unit = sendLocalizedMessage(node, params: _*)

  def <<(text: String): RichPlayer = {
    player.sendMessage(text)
    this
  }

}

object RichPlayer {

  implicit def player2rich(player: Player): RichPlayer = new RichPlayer(player)

  implicit def rich2player(player: RichPlayer): Player = player.player

}
