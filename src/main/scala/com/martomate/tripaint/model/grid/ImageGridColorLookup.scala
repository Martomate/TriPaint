package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.coords.{GlobalPixCoords, PixelCoords}
import scalafx.scene.paint.Color

class ImageGridColorLookup(grid: ImageGrid) extends ColorLookup {
  override def lookup(coords: GlobalPixCoords): Option[Color] = {
    val pixelCoords = PixelCoords(coords, grid.imageSize)
    grid(pixelCoords.image).map(_.storage(pixelCoords.pix))
  }
}
