package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage

class MotionBlurEffect(radius: Int) extends Effect {
  def name: String = "Motion blur"

  override def action(image: ImageStorage): Unit = {
    import com.martomate.tripaint.image.ExtendedColor._
    if (radius > 0) {
      val radiusSq = radius * radius
      val newVals = for (i <- 0 until image.numPixels) yield {
        val here = image.coordsFromIndex(i)
        val cols = image.search(here, (p, _) => here.y == p.y && math.pow(here.x - p.x, 2) <= radiusSq * 1.5).map(c => {
          (math.exp(-2 * math.pow(here.x - c.x, 2) / radiusSq), image(c.index))
        })
        val col = image(i)
        val numCols = cols.foldLeft(1d)(_ + _._1)
        (cols.foldLeft(col * 1)((now, next) => now + next._2 * next._1) / numCols).toColor
      }
      for (i <- 0 until image.numPixels) image(i) = newVals(i)
    }
  }
}
