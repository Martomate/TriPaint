package tripaint.effects

import tripaint.{Color, ColorLookup, FloodFillSearcher}
import tripaint.coords.{GlobalPixCoords, GridCoords, PixelCoords}
import tripaint.grid.{ImageGrid, ImageGridColorLookup}

abstract class LocalEffect extends Effect {

  protected def predicate(image: ColorLookup, here: GlobalPixCoords)(
      coords: GlobalPixCoords,
      color: Color
  ): Boolean

  protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(
      coords: GlobalPixCoords
  ): (Double, Color)

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    val colorLookup = new ImageGridColorLookup(grid)

    val searcher = new FloodFillSearcher(colorLookup)
    val allChanges = for (imageCoords <- images) yield {
      val image = grid(imageCoords).get.storage

      val newVals = for (here <- image.allPixels) yield {
        val coords = PixelCoords(here, imageCoords)
        val coordsGlobal = coords.toGlobal(grid.imageSize)

        val cols = searcher
          .search(coordsGlobal, predicate(colorLookup, coordsGlobal))
          .map(weightedColor(colorLookup, coordsGlobal))

        val numCols = cols.foldLeft(0d)(_ + _._1)
        here -> (cols.map((w, c) => c * w).reduce(_ + _) / numCols)
      }
      imageCoords -> newVals
    }
    for ((im, vals) <- allChanges) {
      val image = grid(im).get.storage
      for ((coords, color) <- vals) image.setColor(coords, color)
    }
  }
}
