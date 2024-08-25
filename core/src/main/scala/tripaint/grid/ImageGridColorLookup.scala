package tripaint.grid

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.{GlobalPixCoords, PixelCoords}

class ImageGridColorLookup(grid: ImageGrid) extends ColorLookup {
  override def lookup(coords: GlobalPixCoords): Option[Color] = {
    val pixelCoords = PixelCoords(coords, grid.imageSize)
    val img = grid(pixelCoords.image)
    if img != null then {
      Some(img.storage.getColor(pixelCoords.pix))
    } else {
      None
    }
  }
}
