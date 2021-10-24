package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.content.ImageChangeListener
import com.martomate.tripaint.model.coords.TriangleCoords

trait ITriImage extends ImageChangeListener {
  protected def drawTriangle(coords: TriangleCoords): Unit

  def redraw(): Unit

  override final def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit = drawTriangle(coords)

  override final def onImageChangedALot(): Unit = redraw()
}
