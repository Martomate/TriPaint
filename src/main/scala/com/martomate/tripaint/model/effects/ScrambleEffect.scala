package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.GridCoords

import scala.util.Random

object ScrambleEffect extends Effect {
  def name: String = "Scramble"

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    for (imageCoords <- images) {
      val image = grid(imageCoords).get.storage
      val allPixels = image.allPixels

      val transform = allPixels.zip(new Random().shuffle(allPixels).map(image.getColor))

      for ((from, col) <- transform) {
        image.setColor(from, col)
      }
    }
  }
}
