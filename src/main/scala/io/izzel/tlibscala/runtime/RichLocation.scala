package io.izzel.tlibscala.runtime

import org.bukkit.Location

class RichLocation(private val location: Location) {

  def +(loc: (Double, Double, Double)): Location = location.add(loc._1, loc._2, loc._3)

  def -(vec: (Double, Double, Double)): Location = this.+(-vec._1, -vec._2, -vec._3)

  def *(x: Double): Location = location.multiply(x)

  def /(x: Double): Location = this * (1 / x)

}

object RichLocation {

  implicit def Location2rich(Location: Location): RichLocation = new RichLocation(Location)

  implicit def rich2Location(richLocation: RichLocation): Location = richLocation.location

}
