package com.ilummc.tlib.scala

import org.bukkit.plugin.Plugin

object Task {

  def apply(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTask(plugin).getTaskId
  }

  def apply(delay: Long)(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskLater(plugin, delay).getTaskId
  }

  def apply(init: Long, period: Long)(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskTimer(plugin, init, period).getTaskId
  }

  def cancel(): Nothing = throw new CancelException

}
