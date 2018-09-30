package com.ilummc.tlib.scala

import org.bukkit.plugin.Plugin

object AsyncTask {

  def apply(task: => Unit)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskAsynchronously(plugin).getTaskId
  }

  def apply(delay: Long)(task: => Unit)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskLaterAsynchronously(plugin, delay).getTaskId
  }

  def apply(init: Long, period: Long)(task: => Unit)(implicit plugin: Plugin): Int = {
    ScalaTaskExecutor(task).runTaskTimerAsynchronously(plugin, init, period).getTaskId
  }

}
