package com.martomate.tripaint.model

trait Change {
  def undo(): Unit

  def redo(): Unit
}
