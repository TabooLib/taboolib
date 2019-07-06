package io.lzzel.tlibscala

import io.izzel.taboolib.module.locale.TLocale.Logger
import org.bukkit.Bukkit
import org.bukkit.event.{Event, EventException, EventPriority, Listener}
import org.bukkit.plugin.{EventExecutor, Plugin}

object Prelude extends Implicits {

  def listen[T <: Event](clazz: Class[T],
                         ignoreCancelled: Boolean = true,
                         priority: EventPriority = EventPriority.NORMAL)
                        (handler: T => Unit)
                        (implicit plugin: Plugin): Unit = {
    val listener = new SingleListener(handler)
    Bukkit.getPluginManager.registerEvent(clazz, listener, priority, listener, plugin, ignoreCancelled)
  }

  def runTask(task: => Unit)(implicit plugin: Plugin): Unit = Task(task)

  def runTask(delay: Long)(task: => Unit)(implicit plugin: Plugin): Unit = Task(delay)(task)

  def runTask(init: Long, period: Long)(task: => Unit)(implicit plugin: Plugin): Unit = Task(init, period)(task)

  def runTaskAsync(task: => Unit)(implicit plugin: Plugin): Unit = AsyncTask(task)

  def runTaskAsync(delay: Long)(task: => Unit)(implicit plugin: Plugin): Unit = AsyncTask(delay)(task)

  def runTaskAsync(init: Long, period: Long)(task: => Unit)(implicit plugin: Plugin): Unit = AsyncTask(init, period)(task)

  def info(node: String, params: String*): Unit = Logger.info(node, params: _*)

  def error(node: String, params: String*): Unit = Logger.error(node, params: _*)

  def fine(node: String, params: String*): Unit = Logger.fine(node, params: _*)

  def debug(node: String, params: String*): Unit = Logger.verbose(node, params: _*)

}

private[this] class SingleListener[T <: Event](handler: T => Any) extends Listener with EventExecutor {
  override def execute(listener: Listener, event: Event): Unit = {
    try handler(event.asInstanceOf[T]) catch {
      case e: Throwable => throw new EventException(e)
    }
  }
}
