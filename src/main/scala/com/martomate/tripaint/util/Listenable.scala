package com.martomate.tripaint.util

import scala.collection.mutable.ArrayBuffer

/** Like Observable but with custom listener class
  *
  * @tparam L
  *   the listener trait/interface
  */
trait Listenable[L] {
  private val listeners: ArrayBuffer[L] = ArrayBuffer.empty
  final def addListener(listener: L): Unit = listeners += listener
  final def removeListener(listener: L): Unit = listeners -= listener

  final protected def notifyListeners(func: L => Unit): Unit = listeners.foreach(func)
}
