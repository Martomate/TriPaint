package com.martomate.tripaint.model.content

import com.martomate.tripaint.model.ImageChange
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.storage.ImageStorage
import com.martomate.tripaint.model.undo.UndoManager
import scalafx.beans.property.BooleanProperty

class ImageContent(val coords: TriImageCoords, val changeTracker: ImageChangeTracker) {
  def storage: ImageStorage = changeTracker.image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value

  val undoManager = new UndoManager
  def undo(): Unit = undoManager.undo()
  def redo(): Unit = undoManager.redo()

  def applyEffect(effect: Effect, imageGrid: ImageGrid): Unit = {// TODO: This is not registered in the UndoManager
    undoManager.append(ImageChange.makeTotalChange(effect.name, this)(effect.action(coords, imageGrid)))
    changeTracker.tellListenersAboutBigChange()
  }
}
