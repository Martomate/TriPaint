package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords

trait ImageChangeListener {
  def onImageChanged(event: ImageContent.Event): Unit
}
