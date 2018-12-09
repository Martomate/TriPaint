package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.grid.ImageGrid

import scala.util.Random

object ScrambleEffect extends Effect {
  def name: String = "Scramble"

  override def action(imageCoords: TriImageCoords, grid: ImageGrid): Unit = {
    val image = grid(imageCoords).get.storage
    val allPixels = image.allPixels

    val transform = allPixels.zip(new Random().shuffle(allPixels).map(image.apply))

    for ((from, col) <- transform) {
      image(from) = col
    }
  }
}
