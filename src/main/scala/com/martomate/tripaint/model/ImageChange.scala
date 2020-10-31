package com.martomate.tripaint.model

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.undo.Change
import scalafx.scene.paint.Color

class ImageChange(val description: String, val image: ImageContent, pixelsChanged: Seq[PixelChange]) extends Change {
  def redo(): Boolean = {
    val draw = image.storage
    for (ch <- pixelsChanged) draw(ch.coords) = ch.after
    image.changeTracker.tellListenersAboutBigChange()
    true
  }

  def undo(): Boolean = {
    val draw = image.storage
    for (ch <- pixelsChanged) draw(ch.coords) = ch.before
    image.changeTracker.tellListenersAboutBigChange()
    true
  }
}

case class PixelChange(coords: TriangleCoords, before: Color, after: Color)
