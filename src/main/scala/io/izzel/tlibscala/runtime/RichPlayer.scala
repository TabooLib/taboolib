package io.izzel.tlibscala.runtime

import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.compat.PermissionHook
import io.izzel.taboolib.util.lite.Scoreboards
import org.bukkit.entity.Player

class RichPlayer(private val player: Player) extends RichOfflinePlayer(player) {

  def sendActionBar(x: String): Unit = TLocale.Display.sendActionBar(player, x)

  def displaySidebarUnranked(title: String, elements: Array[String]): Unit = Scoreboards.display(player, elements: _*)

  def displaySidebarUnranked(title: String, elements: List[String]): Unit = Scoreboards.display(player, elements: _*)

  def displaySidebarUnranked(title: String, elements: String*): Unit = Scoreboards.display(player, elements: _*)

  def addPermission(perm: String): Unit = PermissionHook.addPermission(player, perm)

  def removePermission(perm: String): Unit = PermissionHook.removePermission(player, perm)

  def sendTitle(title: String, subtitle: String, fadein: Int, stay: Int, fadeout: Int): Unit = TLocale.Display.sendTitle(player, title, subtitle, fadein, stay, fadeout)

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
