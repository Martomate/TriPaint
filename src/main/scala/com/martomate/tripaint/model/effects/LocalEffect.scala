package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.storage.{ImageSearcher, ImageStorage}
import scalafx.scene.paint.Color

abstract class LocalEffect extends Effect {

  def predicate(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): Boolean

  def weightedColor(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): (Double, Color)

  override def action(image: ImageStorage): Unit = {
    import com.martomate.tripaint.model.ExtendedColor._
    val searcher = new ImageSearcher(image)

    val newVals = for (here <- image.allPixels) yield {
      val cols = searcher.search(here, predicate(image, here)).map(weightedColor(image, here))
      val col = image(here)
      val numCols = cols.foldLeft(1d)(_ + _._1)
      here -> (cols.foldLeft(col * 1)((now, next) => now + next._2 * next._1) / numCols).toColor
    }
    for ((coords, color) <- newVals) image(coords) = color
  }
}
