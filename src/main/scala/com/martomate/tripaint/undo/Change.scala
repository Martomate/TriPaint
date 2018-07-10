package com.martomate.tripaint.undo

trait Change {
  def description: String

  def undo: Boolean

  def redo: Boolean
}