package io.izzel.tlibscala

import org.bukkit.plugin.Plugin

object AsyncTask {

  def apply(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskAsynchronously(plugin).getTaskId
  }

  def apply(delay: Long)(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskLaterAsynchronously(plugin, delay).getTaskId
  }

  def apply(init: Long, period: Long)(task: => Any)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskTimerAsynchronously(plugin, init, period).getTaskId
  }
}
