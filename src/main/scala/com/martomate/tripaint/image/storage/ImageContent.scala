package com.martomate.tripaint.image.storage

import scalafx.beans.property.BooleanProperty

class ImageContent(val changeTracker: ImageChangeTracker) {
  def storage: ImageStorage = changeTracker.image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value
}
