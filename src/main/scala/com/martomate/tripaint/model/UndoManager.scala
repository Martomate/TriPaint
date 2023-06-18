package com.martomate.tripaint.model

import com.martomate.tripaint.model.Change

import scala.collection.mutable.ArrayBuffer

class UndoManager {
  private val changes = ArrayBuffer.empty[Change]
  private var redoIndex = 0

  private def canUndo: Boolean = redoIndex > 0

  def undo(): Boolean =
    if canUndo then
      redoIndex -= 1
      changes(redoIndex).undo()
      true
    else false

  private def canRedo: Boolean = redoIndex <= changes.size - 1

  def redo(): Boolean =
    if canRedo then
      redoIndex += 1
      changes(redoIndex - 1).redo()
      true
    else false

  def append(change: Change): Unit =
    if (canRedo) changes.remove(redoIndex, changes.size - redoIndex)
    changes.append(change)
    redoIndex += 1
}
