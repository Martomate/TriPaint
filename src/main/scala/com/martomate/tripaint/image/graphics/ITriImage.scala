package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.content.ImageChangeListener
import com.martomate.tripaint.image.coords.TriangleCoords
import scalafx.scene.paint.Color

trait ITriImage extends ImageChangeListener {
  protected def drawTriangle(coords: TriangleCoords): Unit

  def redraw(): Unit

  override def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit = drawTriangle(coords)

  override def onImageChangedALot(): Unit = redraw()
}
