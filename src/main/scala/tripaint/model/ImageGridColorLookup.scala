package tripaint.model

import tripaint.model.coords.{GlobalPixCoords, PixelCoords}

class ImageGridColorLookup(grid: ImageGrid) extends ColorLookup {
  override def lookup(coords: GlobalPixCoords): Option[Color] = {
    val pixelCoords = PixelCoords(coords, grid.imageSize)
    grid(pixelCoords.image).map(_.storage.getColor(pixelCoords.pix))
  }
}
