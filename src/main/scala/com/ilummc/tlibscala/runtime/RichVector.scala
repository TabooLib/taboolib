package com.ilummc.tlibscala.runtime

import org.bukkit.util.Vector

class RichVector(private val vector: Vector) {

  def +(vec: (Double, Double, Double)): Vector =
    vector.setX(vector.getX + vec._1).setY(vector.getY + vec._2).setZ(vector.getZ + vec._3)


  def -(vec: (Double, Double, Double)): Vector = this.+(-vec._1, -vec._2, -vec._3)

  def *(x: Double): Vector = vector.multiply(x)

  def /(x: Double): Vector = this * (1 / x)

}

object RichVector {

  implicit def vector2rich(vector: Vector): RichVector = new RichVector(vector)

  implicit def rich2vector(richVector: RichVector): Vector = richVector.vector

}
