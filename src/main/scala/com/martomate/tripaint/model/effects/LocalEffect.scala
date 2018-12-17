package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.{GlobalPixCoords, PixelCoords, TriImageCoords}
import com.martomate.tripaint.model.grid.{ColorLookup, ImageGrid, ImageGridColorLookup, ImageGridSearcher}
import scalafx.scene.paint.Color

abstract class LocalEffect extends Effect {

  protected def predicate(image: ColorLookup, here: GlobalPixCoords)(coords: GlobalPixCoords, color: Color): Boolean

  protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(coords: GlobalPixCoords): (Double, Color)

  override def action(images: Seq[TriImageCoords], grid: ImageGrid): Unit = {
    import com.martomate.tripaint.model.ExtendedColor._

    val colorLookup = new ImageGridColorLookup(grid)

    val searcher = new ImageGridSearcher(colorLookup)
    val allChanges = for (imageCoords <- images) yield {
      val image = grid(imageCoords).get.storage

      val newVals = for (here <- image.allPixels) yield {
        val coords = PixelCoords(here, imageCoords)
        val coordsGlobal = coords.toGlobal(grid.imageSize)
        val cols = searcher.search(coordsGlobal, predicate(colorLookup, coordsGlobal)).map(weightedColor(colorLookup, coordsGlobal))

        val numCols = cols.foldLeft(0d)(_ + _._1)
        here -> (cols.foldLeft(Color.Black * 1)((now, next) => now + next._2 * next._1) / numCols).toColor
      }
      imageCoords -> newVals
    }
    for ((im, vals) <- allChanges) {
      val image = grid(im).get.storage
      for ((coords, color) <- vals) image(coords) = color
    }
  }
}
