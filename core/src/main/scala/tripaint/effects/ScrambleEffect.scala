package tripaint.effects

import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid

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
