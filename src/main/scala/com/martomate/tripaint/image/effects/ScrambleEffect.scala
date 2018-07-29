package com.martomate.tripaint.image.effects

import com.martomate.tripaint.image.storage.ImageStorage

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
