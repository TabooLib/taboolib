package com.ilummc.tlibscala

import com.ilummc.tlibscala.runtime.{RichLocation, RichOfflinePlayer, RichPlayer, RichVector}
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.{Location, OfflinePlayer, World, util}

abstract class Implicits {

  implicit def player2rich(player: Player): RichPlayer = player

  implicit def offline2rich(player: OfflinePlayer): RichOfflinePlayer = player

  implicit def tuple2location(loc: (World, Double, Double, Double)): Location = new Location(loc._1, loc._2, loc._3, loc._4)

  implicit def tuple2vector(vec: (Double, Double, Double)): Vector = new util.Vector(vec._1, vec._2, vec._3)

  implicit def location2tuple(loc: Location): (Double, Double, Double) = (loc.getX, loc.getY, loc.getZ)

  implicit def vector2tuple(vec: Vector): (Double, Double, Double) = (vec.getX, vec.getY, vec.getZ)

  implicit def location2rich(loc: Location): RichLocation = loc

  implicit def vector2rich(vector: Vector): RichVector = vector

}
