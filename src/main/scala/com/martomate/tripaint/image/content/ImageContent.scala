package com.martomate.tripaint.image.content

import com.martomate.tripaint.image.coords.TriImageCoords
import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.beans.property.BooleanProperty

class ImageContent(val coords: TriImageCoords, val changeTracker: ImageChangeTracker) {
  def storage: ImageStorage = changeTracker.image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value
}
