package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.{PixelCoords, TriImageCoords}
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridSearcher}
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

abstract class LocalEffect extends Effect {

  def predicate(image: ImageStorage, here: PixelCoords)(coords: PixelCoords, color: Color): Boolean

  def weightedColor(image: ImageStorage, here: PixelCoords)(coords: PixelCoords): (Double, Color)

  override def action(imageCoords: TriImageCoords, grid: ImageGrid): Unit = {
    import com.martomate.tripaint.model.ExtendedColor._

    val searcher = new ImageGridSearcher(grid)
    val image = grid(imageCoords).get.storage

    val newVals = for (here <- image.allPixels) yield {
      val coords = PixelCoords(here, imageCoords)
      val cols = searcher.search(coords, predicate(image, coords)).map(weightedColor(image, coords))
      val col = image(here)
      val numCols = cols.foldLeft(1d)(_ + _._1)
      here -> (cols.foldLeft(col * 1)((now, next) => now + next._2 * next._1) / numCols).toColor
    }
    for ((coords, color) <- newVals) image(coords) = color
  }
}
