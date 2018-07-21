package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.scene.paint.Color

trait Effect {
  def name: String
  def action(image: ImageStorage): Unit
}

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

object PerlinNoiseEffect extends Effect {
  def name: String = "Perlin noise"

  override def action(image: ImageStorage): Unit = ???
}

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(image: ImageStorage): Unit = {
    for (i <- 0 until image.numPixels) {
      image(i) = Color.hsb(
        math.random * (max.hue - min.hue) + min.hue,
        math.random * (max.saturation - min.saturation) + min.saturation,
        math.random * (max.brightness - min.brightness) + min.brightness,
        1
      )
    }
  }
}

object ScrambleEffect extends Effect {
  def name: String = "Scramble"

  override def action(image: ImageStorage): Unit = {
    for (i <- 0 until image.numPixels) {
      val idx = (math.random * image.numPixels).toInt
      val temp = image(i)
      image(i) = image(idx)
      image(idx) = temp
    }
  }
}