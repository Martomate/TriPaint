package com.martomate.tripaint.model

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.undo.Change
import scalafx.scene.paint.Color

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

