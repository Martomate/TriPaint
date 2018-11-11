package com.martomate.tripaint.image

import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.graphics.TriImage
import com.martomate.tripaint.undo.Change
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

class ImageChange(val description: String, val image: TriImage, pixelsChanged: Seq[(TriangleCoords, Color, Color)]) extends Change {
  def redo(): Boolean = {
    val draw = image.content.storage
    for ((index, _, newColor) <- pixelsChanged) draw(index) = newColor
    image.content.changeTracker.tellListenersAboutBigChange()
    true
  }

  def undo(): Boolean = {
    val draw = image.content.storage
    for ((index, oldColor, _) <- pixelsChanged) draw(index) = oldColor
    image.content.changeTracker.tellListenersAboutBigChange()
    true
  }
}

private[image] class CumulativeImageChange {
  private val pixelsChanged = ArrayBuffer.empty[(TriangleCoords, Color, Color)]

  def done(description: String, image: TriImage): ImageChange = {
    val change = new ImageChange(description, image, pixelsChanged.reverse.toVector)
    pixelsChanged.clear
    change
  }

  def addChange(index: TriangleCoords, oldColor: Color, newColor: Color): Unit = addChange(change = (index, oldColor, newColor))

  def addChange(change: (TriangleCoords, Color, Color)): Unit = pixelsChanged += change
}
