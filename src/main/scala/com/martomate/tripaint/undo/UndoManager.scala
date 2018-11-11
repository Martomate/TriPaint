package com.martomate.tripaint.undo

import scala.collection.mutable.ArrayBuffer

class UndoManager {
  private val changes = ArrayBuffer.empty[Change]
  private var redoIndex = 0

  def canUndo: Boolean = redoIndex > 0

  def undo(): Boolean = {
    if (canUndo) {
      redoIndex -= 1
//      println("Undo: " + changes(redoIndex).description)
      changes(redoIndex).undo()
    } else false
  }

  def canRedo: Boolean = redoIndex <= changes.size - 1

  def redo(): Boolean = {
    if (canRedo) {
      redoIndex += 1
//      println("Redo: " + changes(redoIndex - 1).description)
      changes(redoIndex - 1).redo()
    } else false
  }

  def append(change: Change): Unit = {
    if (canRedo) changes.remove(redoIndex, changes.size - redoIndex)
    changes.append(change)
    redoIndex += 1
  }

  def goTo(index: Int): Boolean = {
    val newRedoIndex = index + 1
    if (newRedoIndex > redoIndex) {
      while (newRedoIndex > redoIndex && redo) {}
    } else {
      while (newRedoIndex < redoIndex && undo) {}
    }
    newRedoIndex == redoIndex
  }
}