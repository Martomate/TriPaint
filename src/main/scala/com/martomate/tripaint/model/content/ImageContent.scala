package com.martomate.tripaint.model.content

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.beans.property.BooleanProperty

class ImageContent(val coords: TriImageCoords, val changeTracker: ImageChangeTracker) {
  def storage: ImageStorage = changeTracker.image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value
}
