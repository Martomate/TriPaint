package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.model.undo.UndoManager
import scalafx.beans.property.BooleanProperty

class ImageContent(val coords: TriImageCoords, val changeTracker: ImageChangeTracker) {
  def storage: ImageStorage = changeTracker.image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value

  val undoManager = new UndoManager
  def undo(): Unit = undoManager.undo()
  def redo(): Unit = undoManager.redo()
}
