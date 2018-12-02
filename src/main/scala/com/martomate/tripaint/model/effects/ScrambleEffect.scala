package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.storage.ImageStorage

import scala.util.Random

object ScrambleEffect extends Effect {
  def name: String = "Scramble"

  override def action(image: ImageStorage): Unit = {
    val allPixels = image.allPixels

    val transform = allPixels.zip(new Random().shuffle(allPixels).map(image.apply))

    for ((from, col) <- transform) {
      image(from) = col
    }
  }
}
