package tripaint.grid

import tripaint.{Color, ColorLookup}
import tripaint.coords.{GlobalPixCoords, PixelCoords}

class ImageGridColorLookup(grid: ImageGrid) extends ColorLookup {
  override def lookup(coords: GlobalPixCoords): Option[Color] = {
    val pixelCoords = PixelCoords(coords, grid.imageSize)
    grid(pixelCoords.image).map(_.storage.getColor(pixelCoords.pix))
  }
}
