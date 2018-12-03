package com.martomate.tripaint.model

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.undo.Change
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer

object ImageChange {
  def makeTotalChange(description: String, image: ImageContent)(action: ImageContent => Unit): ImageChange = {
    val allPixels = image.storage.allPixels
    val start = allPixels.map(image.storage(_))
    action(image)
    val end = allPixels.map(image.storage(_))

    val changed = allPixels.indices.map(i => (allPixels(i), start(i), end(i))).filter(p => p._2 != p._3)
    new ImageChange(description, image, changed)
  }
}

class ImageChange(val description: String, val image: ImageContent, pixelsChanged: Seq[(TriangleCoords, Color, Color)]) extends Change {
  def redo(): Boolean = {
    val draw = image.storage
    for ((index, _, newColor) <- pixelsChanged) draw(index) = newColor
    image.changeTracker.tellListenersAboutBigChange()
    true
  }

  def undo(): Boolean = {
    val draw = image.storage
    for ((index, oldColor, _) <- pixelsChanged) draw(index) = oldColor
    image.changeTracker.tellListenersAboutBigChange()
    true
  }
}

class CumulativeImageChange {
  private val pixelsChanged = ArrayBuffer.empty[(TriangleCoords, Color, Color)]

  def done(description: String, image: ImageContent): ImageChange = {
    val change = new ImageChange(description, image, pixelsChanged.reverse.toVector)
    pixelsChanged.clear
    change
  }

  def addChange(index: TriangleCoords, oldColor: Color, newColor: Color): Unit = addChange(change = (index, oldColor, newColor))

  def addChange(change: (TriangleCoords, Color, Color)): Unit = pixelsChanged += change
}
