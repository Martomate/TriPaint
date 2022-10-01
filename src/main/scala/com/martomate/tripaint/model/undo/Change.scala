package com.martomate.tripaint.model.undo

trait Change {
  def description: String

  def undo(): Boolean

  def redo(): Boolean
}
