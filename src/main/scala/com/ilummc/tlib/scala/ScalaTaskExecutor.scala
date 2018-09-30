package com.ilummc.tlib.scala

import org.bukkit.scheduler.BukkitRunnable

private[scala] class ScalaTaskExecutor(task: => Unit) extends BukkitRunnable {

  override def run(): Unit = {
    try task catch {
      case _: CancelException => cancel()
      case e: Throwable => throw e
    }
  }

}

object ScalaTaskExecutor {
  def apply(task: => Unit): ScalaTaskExecutor = new ScalaTaskExecutor(task)
}

class CancelException extends RuntimeException {
  override def getMessage: String = "Uncaught cancel task signal! Any Task.cancel() should only be used in a Task."
}
