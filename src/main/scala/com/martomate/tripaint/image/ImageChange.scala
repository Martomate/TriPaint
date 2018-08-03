package com.martomate.tripaint.image

import com.martomate.tripaint.undo.Change
import scalafx.scene.paint.Color
import scala.collection.mutable.ArrayBuffer

class ImageChange(val description: String, val image: TriImage, pixelsChanged: Seq[(Int, Color, Color)]) extends Change {
  def redo(): Boolean = {
    ???
/*    val draw = image.storage
    val prevReg = draw.registerChanges
    draw.registerChanges = false
    for ((index, _, newColor) <- pixelsChanged) draw(index) = newColor
    draw.registerChanges = prevReg
    image.redraw(false)*/
    true
  }

  def undo(): Boolean = {
    ???
/*    val draw = image.storage
    val prevReg = draw.registerChanges
    draw.registerChanges = false
    for ((index, oldColor, _) <- pixelsChanged) draw(index) = oldColor
    draw.registerChanges = prevReg
    image.redraw(false)*/
    true
  }
}

private[image] class CumulativeImageChange {
  private val pixelsChanged = ArrayBuffer.empty[(Int, Color, Color)]

  def done(description: String, image: TriImage): ImageChange = {
    val change = new ImageChange(description, image, pixelsChanged.reverse.toVector)
    pixelsChanged.clear
    change
  }

  def addChange(index: Int, oldColor: Color, newColor: Color): Unit = addChange(change = (index, oldColor, newColor))

  def addChange(change: (Int, Color, Color)): Unit = pixelsChanged += change
}
