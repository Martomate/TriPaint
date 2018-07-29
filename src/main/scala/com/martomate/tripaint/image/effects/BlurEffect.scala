package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage

class BlurEffect(radius: Int) extends Effect {
  def name: String = "Blur"

  override def action(image: ImageStorage): Unit = {
    import com.martomate.tripaint.image.ExtendedColor._
    if (radius > 0) {
      val radiusSq = radius * radius
      val newVals = for (i <- 0 until image.numPixels) yield {
        val here = image.coordsFromIndex(i)
        val cols = image.search(here, (p, _) => p.distanceSq(here) <= radiusSq * 1.5).map(c => {
          (math.exp(-2 * c.distanceSq(here) / radiusSq), image(c.index))
        })
        val col = image(i)
        val numCols = cols.foldLeft(1d)(_ + _._1)
        (cols.foldLeft(col * 1)((now, next) => now + next._2 * next._1) / numCols).toColor
      }
      for (i <- 0 until image.numPixels) image(i) = newVals(i)
    }
  }
}
