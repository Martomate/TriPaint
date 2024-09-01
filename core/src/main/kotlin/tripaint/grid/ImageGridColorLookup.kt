package tripaint.grid

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import tripaint.coords.PixelCoords

class ImageGridColorLookup(val grid: ImageGrid) : ColorLookup {
    override fun lookup(coords: GlobalPixCoords): Color? {
        val pixelCoords = PixelCoords.from(coords, grid.imageSize)
        val img = grid.apply(pixelCoords.image)

        return img?.storage?.getColor(pixelCoords.pix)
    }
}