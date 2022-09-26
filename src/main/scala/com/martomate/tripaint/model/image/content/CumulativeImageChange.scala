package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords

import scala.collection.mutable.ArrayBuffer

class CumulativeImageChange {
  private val pixelsChanged = ArrayBuffer.empty[PixelChange]

  def done(description: String, image: ImageContent): ImageChange = {
    val change = new ImageChange(description, image, pixelsChanged.reverse.toVector)
    pixelsChanged.clear()
    change
  }

  def addChange(index: TriangleCoords, oldColor: Color, newColor: Color): Unit =
    addChange(PixelChange(index, oldColor, newColor))

  def addChange(change: PixelChange): Unit = pixelsChanged += change
}
